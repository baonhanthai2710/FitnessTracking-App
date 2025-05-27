plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ai_chatbot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ai_chatbot"
        minSdk = 25
        targetSdk = 35
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
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0") // Thêm Core KT X
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Nâng cấp OkHttp

    // Lottie
    implementation("com.airbnb.android:lottie:6.3.0") // Nâng cấp Lottie

    // JSON
    implementation("com.google.code.gson:gson:2.10.1") // Thay thế bằng Gson tốt hơn

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}