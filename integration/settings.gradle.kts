rootProject.name = "integration"
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.mkring.wildlydeplyplugin") {
                useModule("com.mkring.wildlydeplyplugin:wildfly-deploy-gradle-plugin:0.2.10")
            }
        }
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}