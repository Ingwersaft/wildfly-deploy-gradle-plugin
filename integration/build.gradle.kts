import com.mkring.wildlydeplyplugin.DeployWildflyTask
import org.gradle.internal.id.UUIDGenerator
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "0.2.10"
    war
}

group = "com.mkring.wildlydeplyplugin"
version = "1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("org.jboss.spec.javax.servlet:jboss-servlet-api_4.0_spec:1.0.0.Final")
    compileOnly("org.jboss.spec.javax.ws.rs:jboss-jaxrs-api_2.1_spec:1.0.2.Final")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

task("build-id") {
    doFirst {
        File("build.id").apply {
            createNewFile()
            val id = UUIDGenerator().generateId().toString()
            println("buildId=$id")
            writeText(id)
        }
    }
    outputs.doNotCacheIf("always generate new id") { true }
}
task("deploy", DeployWildflyTask::class) {
    host = "localhost"
    port = 9990
    user = "mgmt"
    password = "1234"
    deploymentName = project.name
    runtimeName = "${project.name}-$version.war"
    file = "$buildDir/libs/${project.name}-$version.war".apply { println("file=$this") }
    reload = false
    dependsOn("build-id", "build", "war")
}
tasks.war {
    webInf {
        from("build.id")
    }
}