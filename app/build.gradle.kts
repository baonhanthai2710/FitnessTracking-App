plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    // You will need to choose one namespace and applicationId. Here, both are set to "com.example.ai_chatbot".
    namespace = "com.example.fitnesstrackingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fitnesstrackingapp"
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
    // AndroidX and Material Components
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Lottie
    //implementation("com.airbnb.lottie:lottie:6.3.0")
    //implementation("com.airbnb.lottie:lottie:6.1.0")
    implementation("com.airbnb.android:lottie:5.2.0")
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    //implementation("com.airbnb.lottie:lottie:5.2.0")
    // Firebase BoM (Only declare one, use latest)
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    // Firebase dependencies
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")

    // Thêm App Check
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    // hoặc cho debug
    implementation("com.google.firebase:firebase-appcheck-debug")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}