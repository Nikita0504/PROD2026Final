plugins {
    id("android.fruits.library")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.session"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data.repository)
}
