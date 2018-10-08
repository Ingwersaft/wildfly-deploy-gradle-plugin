# wildfly-deploy-gradle-plugin
Gradle Plugin for deploying Files to Wildfly

:exclamation: Plugin needs some tests for productive use, keep in mind :)

# basic example (gradle kotlin-dsl)
Add deploy-wildfly-plugin to plugins:
```kotlin
plugins {
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "<version>"
}
```
Setup task with configuration:
```kotlin
task("deploy", DeployWildflyTask::class) {
    host = "localhost"
    port = 9090
    user = "mgmt_user"
    password = "mgmt_password"
    deploymentName = project.name                //cli: --name=$runtimeName
    runtimeName = "${project.name}-$version.war" //cli: --runtime-name=$runtimeName
    // filepath, here a war example
    file = "$buildDir/libs/${project.name}-$version.war".apply { println("file=$this") }
}
```

Trigger task: `./gradlew deploy`

If you wan't to deploy to multiple targets you can create multiple tasks like this:
```kotlin
task("deployDev") {
    //config dev
}
task("deployQa") {
    //config qa
}
```

You can also deactivate force and/or reload:
```kotlin
task("deploy") {
        // [...]
        reload = false
        force = false
        // [...]
}
```

You can also undeploy an existing deployment with the identical name beforehand
```kotlin
undeployBeforehand = true
```

You can also restart the wildfly after the deployment (and await it), analogous to the reload mechanism
```kotlin
    reload = false // default for reload is true, so deactivate it first
    restart = true
    awaitRestart = true 
```