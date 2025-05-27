pluginManagement {
    repositories {
        // Kho mặc định để resolve plugins
        google()                                // Google’s Android plugin :contentReference[oaicite:1]{index=1}
        gradlePluginPortal()                    // Gradle Plugin Portal :contentReference[oaicite:2]{index=2}
        mavenCentral()                          // Maven Central :contentReference[oaicite:3]{index=3}

        // JitPack phải dùng cú pháp Kotlin DSL
        maven { url = uri("https://jitpack.io") }  // Đúng: url = uri(...) :contentReference[oaicite:4]{index=4}
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()                                // Google’s Maven repo :contentReference[oaicite:5]{index=5}
        mavenCentral()                          // Maven Central :contentReference[oaicite:6]{index=6}
        // JitPack cho dependencies
        maven { url = uri("https://jitpack.io") }  // Sai nếu không gán = uri :contentReference[oaicite:7]{index=7}
    }
}

rootProject.name = "AI_chatbot"               // File tên đúng *.gradle.kts :contentReference[oaicite:8]{index=8}
include(":app")
