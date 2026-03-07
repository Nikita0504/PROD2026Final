plugins {
    id("android.fruits.library")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.repository"
}

dependencies{
    implementation(project(":core:data:network"))
    implementation(project(":core:data:database"))
    implementation(project(":core:domain"))
}