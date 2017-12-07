import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.mkring.wildlydeplyplugin"
version = "0.1"

plugins {
    kotlin("jvm") version "1.2.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish").version("0.9.9")
}

repositories {
    jcenter()
    mavenCentral()
}

gradlePlugin {
    (plugins) {
        "deploy-wildfly-plugin" {
            id = "com.mkring.wildlydeplyplugin.deploy-wildfly-plugin"
            implementationClass = "com.mkring.wildlydeplyplugin.DeployWildflyPlugin"
        }
    }
}

pluginBundle {
    website = "http://www.gradle.org/"
    vcsUrl = "https://github.com/Ingwersaft/wildfly-deploy-gradle-plugin"
    description = "Deploys files to a Wildfly und reloads it afterwards"
    tags = listOf("deploy", "wildfly", "jboss-as-cli")

    (plugins){
        "deploy-wildfly-plugin" {
            id = "com.mkring.wildlydeplyplugin.deploy-wildfly-plugin"
            displayName = "Wildfly Deploy Gradle Plugin"
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