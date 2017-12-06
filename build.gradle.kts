import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.mkring.wildlydeplyplugin"
version = "1.0.0-SNAPSHOT"


plugins {
    kotlin("jvm") version "1.2.0"
}

repositories {
    jcenter()
    mavenCentral()
}
dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(
            files("jboss-cli-client.jar")
    )
    testCompile(kotlin("test"))
    testCompile(kotlin("test-junit"))
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        kotlinOptions.jvmTarget = "1.8"
    }
}
