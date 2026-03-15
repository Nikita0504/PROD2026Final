plugins {
    id("android.fruits.library")
    id("tech.fruits.room")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.database"
}

dependencies {
    implementation(libs.security.crypto)
}