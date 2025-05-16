// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Remove this line to fix the conflict:
    // id("com.google.gms.google-services") version "4.4.1" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

// You can also remove this entire block if you're using the plugins DSL above
// buildscript {
//     dependencies {
//         classpath("com.google.gms:google-services:4.3.15")
//     }
// }