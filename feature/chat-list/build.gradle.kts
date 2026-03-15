plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.chatlist"
}

dependencies {
    implementation(projects.core.session)
    implementation(projects.core.domain)
    implementation(projects.core.navigation)
}