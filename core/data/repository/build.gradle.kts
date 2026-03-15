plugins {
    id("android.fruits.library")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.repository"
}

dependencies{
    implementation(projects.core.data.network)
    implementation(projects.core.data.database)
    implementation(projects.core.domain)
}