package android

import com.android.build.api.dsl.LibraryExtension
import helpers.configureKotlinAndroid
import org.gradle.kotlin.dsl.getByType

plugins {
    id("com.android.library")
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

configureKotlinAndroid(extensions.getByType<LibraryExtension>())

val path = project.path

dependencies {
    if (path != ":core:debug" && path != ":core:data:network" && path != ":core:domain" && path != ":core:navigation" ) {
        "debugImplementation"(project(":core:debug"))
    }
}