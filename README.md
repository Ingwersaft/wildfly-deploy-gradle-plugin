# wildfly-deploy-gradle-plugin
Gradle Plugin for deploying Files to Wildfly

:exclamation: Plugin needs some tests for productive use, keep in mind :)

## basic example (gradle kotlin-dsl)
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

## gradle groovy example
```
import com.mkring.wildlydeplyplugin.DeployWildflyTask

plugins {
    id("java")
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "0.2.7"
}

group = "x.y"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit:junit:4.12")
}

task deploy(type: DeployWildflyTask) {
    host = "localhost"
    port = 9090
    user = "mgmt_user"
    password = "mgmt_password"
    deploymentName = project.name
    runtimeName = project.name + "-" + version + ".war"
    // filepath, here a war example
    file = file("${buildDir}/libs/${project.name}-${version}.war")
}
```
## gradle kotlin example
```
import com.mkring.wildlydeplyplugin.DeployWildflyTask

plugins {
    java
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "0.2.7"
}

group = "x.y"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

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

# ExecuteWildflyTask - execute commands on the cli
The ExecuteWildflyTask let's you execute commands on the target cli

## groovy example
```
import com.mkring.wildlydeplyplugin.ExecuteWildflyTask

[...]
task wildflyExecute(type: ExecuteWildflyTask) {
    host = "127.0.0.1"
    port = 9990
    user = "testuser"
    password = "123"
    commands = ["deployment-info", "deployment-info"]
}
```

## kotlin example
```
import com.mkring.wildlydeplyplugin.ExecuteWildflyTask

[...]
task("wildflyExecute", ExecuteWildflyTask::class) {
    host = "127.0.0.1"
    port = 9990
    user = "testuser"
    password = "123"
    commands = listOf("deployment-info", "deployment-info")
}

```
