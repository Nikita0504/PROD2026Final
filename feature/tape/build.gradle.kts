plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.tape"
}

dependencies {
    implementation(projects.core.navigation)
    implementation(projects.core.session)
}