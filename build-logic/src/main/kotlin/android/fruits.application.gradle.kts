package android

import com.android.build.api.dsl.ApplicationExtension
import helpers.configureKotlinAndroid
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("com.android.application")
}

val libs = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

android {
    defaultConfig {
        targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

configureKotlinAndroid(extensions.getByType<ApplicationExtension>())

