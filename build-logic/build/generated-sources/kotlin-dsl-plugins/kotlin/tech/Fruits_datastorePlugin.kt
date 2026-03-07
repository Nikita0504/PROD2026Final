package tech


/**
 * Precompiled [fruits.datastore.gradle.kts][tech.Fruits_datastore_gradle] script plugin.
 *
 * @see tech.Fruits_datastore_gradle
 */
public
class Fruits_datastorePlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("tech.Fruits_datastore_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
