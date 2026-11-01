import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
}

fun setting(name: String): String = providers.gradleProperty("source.$name").get()

val apiVersion = setting("apiVersion")
val sourceVersion = setting("version")

// Android also needs a whole number that goes up with each release: 1.2.3 becomes 10203.
val versionNumber =
    Regex("""(\d{1,2})\.(\d{1,2})\.(\d{1,2})""")
        .matchEntire(sourceVersion)
        ?.destructured
        ?.let { (major, minor, patch) -> major.toInt() * 10000 + minor.toInt() * 100 + patch.toInt() }
        ?: error("source.version must be three numbers below 100, such as 1.0.0, not \"$sourceVersion\"")

// The release key, when one is set. CI sets these from repository secrets.
fun secret(name: String): String? = providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }

val keystore = secret("SIGNING_STORE_FILE")

android {
    namespace = setting("id")
    compileSdk = 37

    defaultConfig {
        applicationId = setting("id")
        minSdk = 24
        targetSdk = 37
        versionCode = versionNumber
        versionName = sourceVersion
        manifestPlaceholders["sourceName"] = setting("name")
        manifestPlaceholders["sourceClass"] = setting("class")
        manifestPlaceholders["apiVersion"] = apiVersion.toInt()
    }

    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = secret("SIGNING_STORE_PASSWORD")
                // keytool's default store keeps the key under the same password.
                keyAlias = secret("SIGNING_KEY_ALIAS") ?: "source"
                keyPassword = secret("SIGNING_KEY_PASSWORD") ?: storePassword
            }
        }
    }

    buildTypes {
        release {
            // With R8 on, add a keep rule for source.class. The API's own rules don't reach a compileOnly dependency.
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            // Leaves the commit it was built from out of the APK.
            vcsInfo.include = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Saikou Neo provides both when it loads the source, so the APK doesn't carry them.
    compileOnly("io.github.kemurixd:saikou-neo-extension-api:$apiVersion")
    compileOnly(libs.kotlin.stdlib)

    testImplementation("io.github.kemurixd:saikou-neo-extension-api:$apiVersion")
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
