package android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import helpers.configureAndroidCompose

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

val androidExtension: CommonExtension =
    extensions.findByType<LibraryExtension>()
        ?: extensions.findByType<ApplicationExtension>()
        ?: error("android.fruits.compose требует android.fruits.library или android.fruits.application")

configureAndroidCompose(androidExtension)
