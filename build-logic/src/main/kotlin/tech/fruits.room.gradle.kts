package tech

import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("com.google.devtools.ksp")
    id("androidx.room")
}

val libs = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    "implementation"(libs.findBundle("room-full").get())
    "ksp"(libs.findLibrary("room-compiler").get())
    "testImplementation"(libs.findLibrary("room-testing").get())
}