import java.util.Properties
import org.gradle.api.GradleException

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// android/local.properties is gitignored and is not a Gradle project-property file.
// Read the release signing values here so they are never stored in android/gradle.properties.
val releaseSigning = run {
    val props = Properties()
    val localProperties = rootProject.file("local.properties")
    if (localProperties.isFile) {
        localProperties.inputStream().use { props.load(it) }
    }
    fun value(name: String): String? = props.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }
    val storeFile = value("RELEASE_STORE_FILE") ?: return@run null
    val storePassword = value("RELEASE_STORE_PASSWORD") ?: return@run null
    val keyAlias = value("RELEASE_KEY_ALIAS") ?: return@run null
    val keyPassword = value("RELEASE_KEY_PASSWORD") ?: return@run null
    object {
        val storeFile = storeFile
        val storePassword = storePassword
        val keyAlias = keyAlias
        val keyPassword = keyPassword
    }
}

android {
    namespace = "com.bensbible.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bensbible.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "1.4"
    }

    signingConfigs {
        val signing = releaseSigning
        if (signing != null) {
            create("release") {
                storeFile = file(signing.storeFile)
                storePassword = signing.storePassword
                keyAlias = signing.keyAlias
                keyPassword = signing.keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val signing = releaseSigning
            if (signing != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("sh.calvin.reorderable:reorderable:2.1.0")
}

if (releaseSigning == null) {
    gradle.taskGraph.whenReady {
        val signsRelease = allTasks.any { it.name == "bundleRelease" || it.name == "assembleRelease" }
        if (signsRelease) {
            throw GradleException(
                "Release signing is not configured. Add RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, and RELEASE_KEY_PASSWORD to android/local.properties."
            )
        }
    }
}
