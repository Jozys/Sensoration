import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val localProperties = readProperties(file("$rootDir/local.properties"))

fun readProperties(propertiesFile: File) = Properties().apply {
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { fis ->
            load(fis)
        }
    }
}

fun generateVersion(
    hotfix: Int = localProperties.getProperty("hotfix.id")?.toInt() ?: 0
): String {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    val date = Date()
    return "${dateFormat.format(date)}.$hotfix"
}

android {
    namespace = "de.schuettslaar.sensoration"
    compileSdk = 35

    val version = System.getenv("VERSION_CODE") ?: generateVersion()

    androidResources {
        //noinspection MissingResourcesProperties
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "de.schuettslaar.sensoration"
        minSdk = 26
        targetSdk = 35
        versionCode = version.replace(".", "").toInt()
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "@string/app_name_debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Jetpack Compose integration
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Material Design 3
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // User Interface
    implementation(libs.charty)

    // Nearby Connections
    implementation(libs.play.services.nearby)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Android Datastore
    implementation(libs.androidx.datastore.preferences)

    // Preferences / Settings
    implementation(libs.compose.prefs3)

    // Utility
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.commons.collections4)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}