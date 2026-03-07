package android

import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val libs = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

dependencies {
    "testImplementation"(libs.findBundle("unit-test").get())
    "androidTestImplementation"(libs.findBundle("android-test").get())
}

