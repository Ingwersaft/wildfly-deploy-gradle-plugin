import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.mkring.wildlydeplyplugin"
version = "0.2.12"

plugins {
    kotlin("jvm") version "1.2.61"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish").version("0.10.0")
}
val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion

repositories {
    jcenter()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("deploy-wildfly-plugin") {
            id = "com.mkring.wildlydeplyplugin.deploy-wildfly-plugin"
            implementationClass = "com.mkring.wildlydeplyplugin.DeployWildflyPlugin"
            displayName = "Wildfly Deploy Gradle Plugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/Ingwersaft/wildfly-deploy-gradle-plugin"
    vcsUrl = "https://github.com/Ingwersaft/wildfly-deploy-gradle-plugin"
    description = "Deploys files to a Wildfly und reloads it afterwards"
    tags = listOf("deploy", "wildfly", "jboss-as-cli")
}

dependencies {
    compile(kotlin("stdlib-jdk8", kotlinVersion))
    runtime(kotlin("reflect", kotlinVersion))
    compile("org.wildfly", "wildfly-cli", "8.2.1.Final")

    compileOnly(gradleApi())

    compile("org.slf4j:slf4j-api:1.7.25")

    testCompile(kotlin("test", kotlinVersion))
    testCompile(kotlin("test-junit", kotlinVersion))

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
