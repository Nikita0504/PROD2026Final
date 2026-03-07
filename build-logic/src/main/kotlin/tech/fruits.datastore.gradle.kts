package tech

import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val libs = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

dependencies {
    "implementation"(libs.findLibrary("datastore-preferences").get())
    "implementation"(libs.findLibrary("coroutines-core").get())
}

