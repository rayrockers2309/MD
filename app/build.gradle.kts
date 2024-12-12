
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.laila.sustainwise"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.testprojectAPI"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Menggunakan sintaks Kotlin untuk memuat properties
        val properties = Properties()
        properties.load(rootProject.file("local.properties").inputStream())
        buildConfigField("String", "SUS_API", properties.getProperty("SUS_API")
        )

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // AndroidX Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.work.runtime)

    // Material Design
    implementation(libs.material)

    // Networking Libraries
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    implementation(libs.volley)

    // Firebase Libraries
    implementation(libs.firebase.bom)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.core)
    implementation(libs.firebase.firestore.ktx.v2511)

    // Google Play Services
    implementation(libs.gms.play.services.auth)
    implementation(libs.play.services.base)

    // Image Loading (Add Glide dependency)
    implementation("com.github.bumptech.glide:glide:4.15.1")  // Or the latest version
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Other Libraries
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    implementation ("com.google.android.material:material:1.7.0")
}





