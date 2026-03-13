package helpers

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    val libs = extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")

    commonExtension.buildFeatures.apply {
        compose = true
    }

    dependencies {
        val bom = libs.findLibrary("compose-bom").get()
        "implementation"(platform(bom))
        "androidTestImplementation"(platform(bom))
        "implementation"(libs.findLibrary("navigation-compose").get())
        "implementation"(libs.findBundle("compose-core").get())
        "implementation"(libs.findBundle("async-image").get())
        "debugImplementation"(libs.findLibrary("compose-ui-tooling").get())
        "debugImplementation"(libs.findLibrary("compose-ui-test-manifest").get())
    }
}
