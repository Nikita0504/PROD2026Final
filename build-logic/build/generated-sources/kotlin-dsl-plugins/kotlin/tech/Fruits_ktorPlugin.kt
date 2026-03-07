package tech


/**
 * Precompiled [fruits.ktor.gradle.kts][tech.Fruits_ktor_gradle] script plugin.
 *
 * @see tech.Fruits_ktor_gradle
 */
public
class Fruits_ktorPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("tech.Fruits_ktor_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
