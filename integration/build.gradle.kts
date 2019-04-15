import com.mkring.wildlydeplyplugin.DeployWildflyTask
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.gradle.internal.id.UUIDGenerator
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    id("com.mkring.wildlydeplyplugin.deploy-wildfly-plugin") version "0.2.10"
    war
}
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.apache.httpcomponents:httpclient:4.5.8")
    }
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
task("verify") {
    doLast {
        val fileId = File("build.id").readText()
        val apiId =
            HttpClients.createDefault().execute(HttpGet("http://localhost:8080/integration-1/rest/id")).entity.let {
                EntityUtils.toString(it)
            }
        println("fileId=$fileId")
        println("apiId=$apiId")
        if (!fileId.equals(apiId)) {
            throw RuntimeException("fileId.equals(apiId) == false!")
        }
    }
}
task("build-id") {
    File("build.id").apply {
        createNewFile()
        val id = UUIDGenerator().generateId().toString()
        println("buildId=$id")
        writeText(id)
    }
    outputs.doNotCacheIf("always generate new id") { true }
}
tasks.findByPath("build")?.outputs?.cacheIf { false }
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