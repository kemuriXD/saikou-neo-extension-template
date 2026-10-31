import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
}

fun setting(name: String): String = providers.gradleProperty("source.$name").get()

val apiVersion = setting("apiVersion")

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
        versionCode = setting("versionCode").toInt()
        versionName = setting("versionName")
        manifestPlaceholders["sourceName"] = setting("name")
        manifestPlaceholders["sourceClass"] = setting("class")
        manifestPlaceholders["apiVersion"] = apiVersion.toInt()
    }

    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = secret("SIGNING_STORE_PASSWORD")
                keyAlias = secret("SIGNING_KEY_ALIAS")
                keyPassword = secret("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // With R8 on, add a keep rule for source.class. The API's own rules don't reach a compileOnly dependency.
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
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
