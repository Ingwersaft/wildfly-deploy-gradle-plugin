# wildfly-deploy-gradle-plugin
Gradle Plugin for deploying Files to Wildfly

:exclamation: Plugin needs some tests for productive use, keep in mind :)

# basic example (gradle kotlin-dsl)
Add deploy-wildfly-plugin to plugins:
```kotlin
plugins {
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "0.1.4"
}
```
Setup target wildfly:
```kotlin
deployWildfly {
        host = "localhost"
        port = 9090
        user = "mgmt_user"
        password = "mgmt_password"
        // filepath, here a war example
        file = "$buildDir/libs/${project.name}-$version.war".apply { println("file=$this") }
}
```

Trigger plugin: `./gradlew build deployWildfly`

If you wan't to deploy to multiple you can create tasks like this:
```kotlin
task("deployDev") {
    deployWildfly {
        host = "<dev_host>"
        port = 9090
        user = "mgmt_user"
        password = "mgmt_password"
        file = "$buildDir/libs/${project.name}-$version.war".apply { println("file=$this") }
    }
    dependsOn("build", "deployWildfly")
}
```

You can also deactivate force and/or reload:
```kotlin
deployWildfly {
        // [...]
        reload = false
        force = false
        // [...]
}
```