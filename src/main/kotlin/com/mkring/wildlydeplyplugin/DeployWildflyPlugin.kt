package com.mkring.wildlydeplyplugin

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class DeployWildflyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("DeployWildflyPlugin applied")
    }
}

open class DeployWildflyTask(
        @Input var file: String? = null,
        @Input var host: String = "localhost",
        @Input var port: Int = 9090,
        @Input var user: String? = null,
        @Input var password: String? = null,
        @Input var reload: Boolean = true,
        @Input var force: Boolean = true
) : DefaultTask() {

    init {
        group = "help"
        description = "Deploys files to a Wildfly und reloads it afterwards"
        dependsOn("build")
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun deployWildfly() {
        if (file == null || user == null || password == null) {
            println("missing configuration")
            return
        }
        println("deployWildfly: going to deploy $file to $host:$port")
        FileDeployer(file, host, port, user, password, reload, force).deploy()
    }
}