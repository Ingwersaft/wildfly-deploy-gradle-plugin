import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.mkring.wildlydeplyplugin"
version = "1.0.0-SNAPSHOT"


plugins {
    kotlin("jvm") version "1.2.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    jcenter()
    mavenCentral()
}

gradlePlugin {
    (plugins) {
        "myPlugin" {
            id = "my-plugin"
            implementationClass = "plugin.MyPlugin"
        }
    }
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(
            // todo replaye with maven location
            files("jboss-cli-client.jar")
    )

    compileOnly(gradleApi())
    testCompile(kotlin("test"))
    testCompile(kotlin("test-junit"))
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        kotlinOptions.jvmTarget = "1.8"
    }
}
publishing {
    repositories {
        maven(url = "build/lib")
    }
}