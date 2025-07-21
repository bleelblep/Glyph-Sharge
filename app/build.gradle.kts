plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

android {
    namespace = "com.bleelblep.glyphzenredesign"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bleelblep.glyphzenredesign"
        minSdk = 34
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Enable vector drawables support
        vectorDrawables {
            useSupportLibrary = true
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
            
            // Enable additional optimizations
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3
            
            // Performance optimizations
            ndk {
                debugSymbolLevel = "NONE"
            }
        }
        debug {
            // Optimize debug builds for better performance during development
            isMinifyEnabled = false
            renderscriptOptimLevel = 1
            
            // Speed up debug builds
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        // Enhanced Kotlin compiler optimizations for performance
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xuse-experimental=kotlin.Experimental",
            "-Xjvm-default=all",
            "-Xuse-ir=true",
            "-Xstring-concat=inline"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
        // Enable Compose compiler optimizations
        freeCompilerArgs += listOf(
            "-Xopt-in=androidx.compose.runtime.ExperimentalComposeApi",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Material Icons Extended - provides access to a much larger set of Material Design icons
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Lifecycle ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Nothing Glyph SDK
    implementation(files("libs/KetchumSDK_Community_20250319.jar"))
    
    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Lottie Animation for Compose – used in new onboarding flow
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    
    // Note: Using built-in Android MediaSession API (no additional dependencies needed)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}