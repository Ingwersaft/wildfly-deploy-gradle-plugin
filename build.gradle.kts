import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.mkring.wildlydeplyplugin"
version = "1.0.0"

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
        "deploy-wildfly-plugin" {
            id = "deploy-wildfly-plugin"
            implementationClass = "com.mkring.wildlydeplyplugin.DeployWildflyPlugin"
        }
    }
}

dependencies {
    compile(kotlin("stdlib-jdk8", "1.2.0"))
    compile(kotlin("reflect", "1.2.0"))
    compile("org.jboss.as", "jboss-as-cli", "7.2.0.Final")

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
        mavenLocal()
        maven(url = "build/lib")
    }
}