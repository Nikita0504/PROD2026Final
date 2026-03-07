package android


/**
 * Precompiled [fruits.compose.gradle.kts][android.Fruits_compose_gradle] script plugin.
 *
 * @see android.Fruits_compose_gradle
 */
public
class Fruits_composePlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("android.Fruits_compose_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
