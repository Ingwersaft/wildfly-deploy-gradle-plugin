package com.mkring.wildlydeplyplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory

open class ExecuteWildflyTask : DefaultTask() {
    val log = LoggerFactory.getLogger(ExecuteWildflyTask::class.java)
    @Input
    var host: String = "localhost"
    @Input
    var port: Int = 9090
    @Input
    var user: String? = null
    @Input
    var password: String? = null
    @Input
    var commands: List<String>? = null

    init {
        group = "help"
        description = "Executes cli commands on a Wildfly"
        dependsOn("build")
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun executeWildfly() {
        if (user == null || password == null || commands == null || commands?.isEmpty() == true) {
            log.error("ExecuteWildflyTask: missing configuration")
            return
        }
        log.info("ExecuteWildflyTask: on $host:$port going to execute:")
        commands?.forEach {
            log.debug("command: `$it`")
        }
        try {
            CliExecutioner.execute(host, port, user, password, commands ?: emptyList())
        } catch (e: Exception) {
            log.error("ExecuteWildflyTask task failed: ${e.message}", e)
            throw e
        }
    }
}