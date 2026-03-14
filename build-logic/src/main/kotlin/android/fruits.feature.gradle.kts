package android

import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("android.fruits.library")
    id("android.fruits.compose")
    id("tech.fruits.koin")
}

val libs = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    "implementation"(libs.findLibrary("navigation-compose").get())
    "implementation"(libs.findLibrary("lifecycle-viewmodel-compose").get())
    "implementation"(libs.findLibrary("lifecycle-runtime-compose").get())
    "implementation"(libs.findLibrary("compose-icons-extended").get())
    if (project.path != ":core:navigation") {
        "debugImplementation"(project(":core:navigation"))
    }
}

