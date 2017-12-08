package com.mkring.wildlydeplyplugin

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

open class DeployWildflyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            configurations.maybeCreate("deployWildfly")
                    .setVisible(false).description = "Deploy Wildfly Configuration"

            extensions.create("deployWildfly", DeployWildplyPluginExtension::class.java)
            tasks.create("deployWildfly", DeployWildflyTask::class.java)
        }
    }

}

open class DeployWildplyPluginExtension {
    var file: String? = null
    var host: String = "localhost"
    var port: Int = 9090
    var user: String? = null
    var password: String? = null
    var reload: Boolean = true
    var force: Boolean = true
}

open class DeployWildflyTask : DefaultTask() {

    init {
        group = "help"
        description = "Deploys files to a Wildfly und reloads it afterwards"
        dependsOn("build")
    }

    @TaskAction
    fun deployWildfly() {
        with(project) {
            val extension = extensions.getByName("deployWildfly") as DeployWildplyPluginExtension
            if (extension.file == null || extension.user == null || extension.password == null) {
                println("missing configuration")
                return
            }
            with(extension) {
                println("deployWildfly: going to deploy $file to $host:$port")
            }
            FileDeployer(extension).deploy()
        }
    }
}