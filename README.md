# wildfly-deploy-gradle-plugin
Gradle Plugin for deploying Files to Wildfly

:exclamation: Plugin needs some tests for productive use, keep in mind :)

***Compatibility:***
```
Successfully tested/known to work: Wildfly 10, Wildfly 14, Wildfly 15
Jboss7: Use <version>-jboss7
Wildfly 15: Use <version>-wildfly15
```
If you have problems using cli batches, try the `-wildfly15` branch (see: #12).

Since version `0.3.0` the api changed slightly, please consule [Do not depend on build task, lazily resolve file #22](https://github.com/Ingwersaft/wildfly-deploy-gradle-plugin/pull/22) 
if you need details.

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
    deploymentName.set(project.name)                //cli: --name=$runtimeName
    runtimeName.set(tasks.war.get().archiveFileName) //cli: --runtime-name=$runtimeName
    // filepath, here a war example
    file.set(tasks.war.get().archiveFile)
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

You can also deploy to WildFly in domain mode, but only one server group per task
```kotlin
    domainServerGroup = "main-server-group"
```

## gradle groovy example
```
import com.mkring.wildlydeplyplugin.DeployWildflyTask

plugins {
    id("java")
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "0.2.10"
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
    file = "${buildDir}/libs/${project.name}-${version}.war"
}

task deployDomain(type: DeployWildflyTask) {
    host = deploy.host
    port = deploy.port
    user = deploy.user
    password = deploy.password
    deploymentName = project.name
    runtimeName = tasks.war.archiveFileName
    file = tasks.war.archiveFile
    // redeploy if artifact with same name already deployed
    undeployBeforehand = true
    // server group of domain mode
    domainServerGroup = "main-server-group"
    // ask to restart servers after deploy instead of only reload them
    restart = true
    reload = false
}
```
## gradle kotlin example
```
import com.mkring.wildlydeplyplugin.DeployWildflyTask

plugins {
    java
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "0.2.10"
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
    deploymentName.set(project.name)                 //cli: --name=$runtimeName
    runtimeName.set(tasks.war.get().archiveFileName) //cli: --runtime-name=$runtimeName
    // Using war.archiveFile will make the deploy task depend on the war task implicitly, no need for dependsOn("war")
    file.set(tasks.war.get().archiveFile)
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

# Build and use locally build wildfly-deploy-gradle-plugin
Building and publishing can be done with:
```bash
$ ./gradlew clean build publish
```

This will deploy the locally build plugin jar into your local maven repo and also into `build/lib`.

To use your locally build plugin you can just
[override the plugin resolutionStrategy](https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_resolution_rules)
inside your settings.gradle(.kts) file.

## example
build.gradle:
```groovy
import com.mkring.wildlydeplyplugin.ExecuteWildflyTask

plugins {
    id("java")
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "0.2.8"
}
task executeCommands(type: ExecuteWildflyTask) {
    host = "localhost"
    port = 9990
    user = "testuser"
    password = "1234"
    commands = ["ls"]
}
```
settings.gradle:
```groovy
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") {
                useModule("com.mkring.wildlydeplyplugin:wildfly-deploy-gradle-plugin:0.2.9") //adapt version if needed
            }
        }
    }
    repositories {
        maven {
            // this is the build folder of your local wildfly-deploy-gradle-plugin repository, you might need to adapt this
            url = uri("build/lib")
        }
        // or
        mavenLocal()

        //
        gradlePluginPortal()
    }
}
```
