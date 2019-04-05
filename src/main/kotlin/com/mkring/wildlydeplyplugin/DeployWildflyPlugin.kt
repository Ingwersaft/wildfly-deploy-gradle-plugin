package com.mkring.wildlydeplyplugin

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory

open class DeployWildflyPlugin : Plugin<Project> {
    val log = LoggerFactory.getLogger(DeployWildflyPlugin::class.java)
    override fun apply(project: Project) {
        log.debug("DeployWildflyPlugin applied")
    }
}

open class DeployWildflyTask : DefaultTask() {
    val log = LoggerFactory.getLogger(DeployWildflyTask::class.java)

    @Input
    var file: String? = null

    @Input
    var domainServerGroup: String = ""

    @Input
    var deploymentName: String? = null
    @Input
    var runtimeName: String? = null

    @Input
    var host: String = "localhost"
    @Input
    var port: Int = 9090
    @Input
    var user: String? = null
    @Input
    var password: String? = null

    @Input
    var reload: Boolean = true
    @Input
    var force: Boolean = true

    @Input
    var awaitReload: Boolean = false

    @Input
    var undeployBeforehand: Boolean = false

    @Input
    var restart: Boolean = false
    @Input
    var awaitRestart: Boolean = false

    init {
        group = "help"
        description = "Deploys files to a Wildfly und reloads it afterwards"
        dependsOn("build")
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun deployWildfly() {
        if (file == null || user == null || password == null) {
            log.error("DeployWildflyTask: missing configuration")
            return
        }
        if (reload && restart) {
            log.error("reload && restart are mutually exclusive!")
            return
        }
        if (awaitReload && awaitRestart) {
            log.error("awaitReload && awaitRestart are mutually exclusive!")
            return
        }
        if (awaitReload && reload.not()) {
            log.warn("awaitReload is pointless if no reload is set")
        }
        if (awaitRestart && restart.not()) {
            log.warn("awaitRestart is pointless if no restart is set")
        }
        log.info("deployWildfly: going to deploy $file to $host:$port")
        try {
            FileDeployer(
                file,
                host,
                port,
                user,
                password,
                reload,
                force,
                deploymentName,
                runtimeName,
                domainServerGroup,
                awaitReload,
                undeployBeforehand,
                restart,
                awaitRestart
            ).deploy()
        } catch (e: Exception) {
            log.error("deployWildfly task failed: ${e.message}", e)
            throw e
        }
    }
}
