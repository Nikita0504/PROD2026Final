plugins {
    id("android.fruits.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.fruits.navigation"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}