plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.ksp)

    alias(libs.plugins.hilt)
}

android {
    namespace = "com.set.patchchanger"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.set.patchcahnger"
        minSdk = 24
        targetSdk = 36
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //Dagger-Hilt DI
    implementation(libs.hilt.android)
    //    kapt(libs.hilt.compiler)
    ksp(libs.hilt.compiler)

    implementation(libs.hilt.navigation.compose)
    //Material3 Extended pack
    implementation(libs.material.icons.extended)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler) // Changed from kapt to ksp

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    // Coroutines for Android (for Dispatchers.Main, lifecycle, etc.)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    // (Optional) Core DataStore library — only needed for advanced usage
    implementation("androidx.datastore:datastore-core:1.1.1")
}