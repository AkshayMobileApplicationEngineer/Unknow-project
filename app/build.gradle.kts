plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("D:\\unknowproject\\unknow.jks")
            keyPassword = "password@123#"
            storePassword = "password@123#"
            keyAlias = "key0"
        }
    }

    namespace = "com.app.unknowproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.unknowproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Define the production URL for the release build
            buildConfigField("String", "BASE_URL", "\"https://api.production.com\"")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            // Define the testing URL for the debug build
            buildConfigField("String", "BASE_URL", "\"https://api.testing.com\"")
        }
    }

    buildFeatures {
        buildConfig = true  // Ensure BuildConfig generation is enabled
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
