plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.newhtcdridi.weather"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.newhtcdridi.weather"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // No runtime dependencies on purpose: the app uses only the Android framework
    // (HttpURLConnection + org.json) so the APK stays tiny and the build stays simple.
    testImplementation("junit:junit:4.13.2")
    // Real org.json implementation for local unit tests (android.jar only ships stubs).
    testImplementation("org.json:json:20240303")
}
