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

// The logo, if there's an icon.png next to gradle.properties. It becomes the APK's icon, where Saikou Neo reads it.
val iconFile: RegularFile = rootProject.layout.projectDirectory.file("icon.png")
val hasIcon = iconFile.asFile.exists()

/** Checks icon.png against what Saikou Neo will show, then copies it in as the drawable `source_icon`. */
abstract class SourceIcon : DefaultTask() {
    @get:InputFile
    abstract val icon: RegularFileProperty

    @get:OutputDirectory
    abstract val res: DirectoryProperty

    @TaskAction
    fun copy() {
        val png = icon.get().asFile.readBytes()
        val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        fun side(at: Int) = (0 until 4).fold(0L) { value, i -> (value shl 8) or (png[at + i].toLong() and 0xFF) }
        check(png.size >= 24 && png.copyOf(8).contentEquals(signature)) { "icon.png must be a PNG file" }
        check(png.size <= 256 * 1024) { "icon.png must be 256 KB or less, but it's ${png.size / 1024} KB" }
        check(side(16) in 1..1024 && side(20) in 1..1024) { "icon.png must be at most 1024 x 1024 pixels" }
        val folder = res.get().dir("drawable-nodpi").asFile
        folder.deleteRecursively()
        folder.mkdirs()
        folder.resolve("source_icon.png").writeBytes(png)
    }
}

val sourceIcon =
    tasks.register<SourceIcon>("sourceIcon") {
        icon.set(iconFile)
    }

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
        manifestPlaceholders["sourceIcon"] = if (hasIcon) "@drawable/source_icon" else "@null"
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

androidComponents {
    onVariants { variant ->
        if (hasIcon) variant.sources.res?.addGeneratedSourceDirectory(sourceIcon, SourceIcon::res)
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
