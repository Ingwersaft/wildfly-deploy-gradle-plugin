package com.mkring.wildlydeplyplugin

import org.jboss.`as`.cli.CommandLineException
import org.jboss.`as`.cli.scriptsupport.CLI
import org.jboss.dmr.ModelNode
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val log = LoggerFactory.getLogger(FileDeployer::class.java)

class FileDeployer(
    private val file: File,
    private val host: String,
    private val port: Int,
    private val user: String?,
    private val password: String?,
    private val reload: Boolean,
    private val force: Boolean,
    private val name: String?,
    private val runtimeName: String?,
    private val domainServerGroup: String,
    private val awaitReload: Boolean,
    private val undeployBeforehand: Boolean,
    private val restart: Boolean,
    private val awaitRestart: Boolean
) {
    private val domainMode = !domainServerGroup.isNullOrEmpty()
    private val deploymentInfoCmd : String = if (domainMode) {
        "deployment-info --server-group=$domainServerGroup"
    } else {
        "deployment-info"
    }
    private val deployCmd : String = if (domainMode) {
        "deploy --server-groups=$domainServerGroup"
    } else {
        if (force) {
            "deploy --force"
        } else {
            "deploy"
        }
    }
    private val enableDeploymentCmd = deployCmd.replace("--force","")
    private val undeployCmd : String = if (domainMode) {
        "undeploy --server-groups=$domainServerGroup"
    } else {
        "undeploy"
    }
    private val restartCmd : String = if (domainMode) {
        "/server-group=$domainServerGroup :restart-servers"
    } else {
        "shutdown --restart=true"
    }
    private val reloadCmd : String = if (domainMode) {
        "/server-group=$domainServerGroup :reload-servers"
    } else {
        "reload"
    }

    
    fun deploy() {
        log.debug("deploy(): $this")
        checkHostDns(host)
        checkSocket(host, port)
        CLI.newInstance().let { cli ->
            log.debug("wildfly connect with $user on $host:$port")
            connect(cli, host, port, user, password)
            log.debug("connected successfully")

            val name = if (name != null) {
                "--name=$name"
            } else {
                ""
            }
            val runtimeName = if (runtimeName != null) {
                "--runtime-name=$runtimeName"
            } else {
                ""
            }

            val deploymentExists = file.isFile
            log.debug("given $file existent: $deploymentExists")
            if (deploymentExists.not()) throw IllegalStateException("couldn't find given deployment")

            if (undeployBeforehand) {
                log.info("undeploying existing deployment with same name if present...")
                var deployments = blockingCmd("$deploymentInfoCmd $name", 2, ChronoUnit.MINUTES).response.get("result").asList()
                var shouldUndeploy = if (domainMode) {
                    //TIP: deployment-info in domain mode has a different response data structure as following
                    // result => [ ("step-1") => { "result" => { "deployment.war" => ... }}, ("step-2") => ... ]
                    (deployments.size > 0).and(deployments.first().asProperty().value.get("result").asList().any {
                        it.asProperty().name == name.removePrefix("--name=") })
                } else {
                    //TIP: deployment-info in standalone mode has simplest response with "result" map only
                    deployments.any { it.asProperty().name == name.removePrefix("--name=") }
                }
                log.debug("shouldUndeploy=$shouldUndeploy")
                if (shouldUndeploy) {
                    blockingCmd("$undeployCmd $name", 2, ChronoUnit.MINUTES).response.also {
                        println("undeploy response: $it\n")
                    }
                }
            }

            // deploy
            val deploySuccess = cli.cmd("$deployCmd $name $runtimeName $file").isSuccess
            log.debug("deploy success: $deploySuccess")

            enableDeploymentIfNecessary(name)

            if (reload) {
                try {
                    log.debug("going to reload wildfly")
                    val reloadSuccess = cli.cmd("$reloadCmd").isSuccess
                    log.debug("reload success: $reloadSuccess")
                } catch (e: CommandLineException) {
                    log.debug("looks like reload timed out: ${e.message}")
                }
            }
            if (restart) {
                try {
                    log.debug("going to restart wildfly")
                    val restartSuccess = cli.cmd("$restartCmd").isSuccess
                    log.debug("restart success: $restartSuccess")
                } catch (e: CommandLineException) {
                    log.debug("looks like restart timed out: ${e.message}")
                }
            }
            cli.disconnect()
        }

        if (awaitReload) {
            blockTillCliIsBack()
        }
        if (awaitRestart) {
            blockTillCliIsBack()
        }
    }

    fun blockTillCliIsBack() {
        log.debug("going to block until the reload/restart finished...\n")
        Thread.sleep(1000)
        val postReloadDeploymentInfoPrettyPrint =
            blockingCmd(deploymentInfoCmd, 1, ChronoUnit.MINUTES).response.responsePrettyPrint()
        log.debug("\n\nPOST reload/restart deployment info:\n$postReloadDeploymentInfoPrettyPrint")
    }

    private fun enableDeploymentIfNecessary(name: String) {
        log.debug("\nchecking if deployment is enabled...")
        val deploymentEnabled =
            blockingCmd(deploymentInfoCmd, 2, ChronoUnit.MINUTES).response.get("result").asList().map {
                it.asProperty().name to it.getParam("enabled").removePrefix("enabled: ")
            }.firstOrNull {
                it.first == name.removePrefix("--name=")
            }?.second?.toBoolean()

        if (deploymentEnabled == false) {
            log.debug("not enabled! going to enable now!")
            blockingCmd("$enableDeploymentCmd $name", 2, ChronoUnit.MINUTES).response.also {
                log.debug("enable response: $it\n")
            }
        }
    }

    /**
     * hacky as hell but works
     */
    private fun blockingCmd(s: String, i: Long, unit: ChronoUnit): CLI.Result {
        val end = LocalDateTime.now().plus(i, unit)
        while (LocalDateTime.now().isBefore(end)) {
            val cli = CLI.newInstance()
            try {
                connect(cli, host, port, user, password)
                log.debug("executing cmd: $s")
                val cmd = cli.cmd(s)
                if (cmd.isSuccess.not()) {
                    throw IllegalStateException("no success")
                }
                return cmd
            } catch (e: Exception) {
                log.debug("connect + cmd exception: ${e::class.java.simpleName}:${e.message}")
                Thread.sleep(500)
                continue
            } finally {
                try {
                    cli.disconnect()
                } catch (e: Exception) {
                    log.debug("disconnect exception: ${e::class.java.simpleName}:${e.message}")
                    throw e
                }
            }
        }
        throw IllegalStateException("can't reconnect after wildfly reload after $i $unit")
    }

    private fun ModelNode.responsePrettyPrint() = get("result").asList().joinToString("\n") {
        "${it.asProperty().name}: " +
                "${it.getParam("enabled")}; " +
                "${it.getParam("runtime-name")}; " +
                "${it.getParam("status")}; " +
                it.getParam("enabled-timestamp")
    }

    private fun ModelNode.getParam(s: String) = "$s: ${get(0).get(s)}"

    override fun toString(): String {
        return "FileDeployer(file=$file, host='$host', port=$port, user=$user, reload=$reload, force=$force, name=$name, runtimeName=$runtimeName, awaitReload=$awaitReload, domainServerGroup=$domainServerGroup)"
    }
}

fun connect(
    cli: CLI,
    host: String,
    port: Int,
    user: String?,
    password: String?
) {
    cli.connect(host, port, user, password?.toCharArray())
}

fun checkHostDns(host: String) {
    log.debug("$host DNS: ${InetAddress.getAllByName(host).joinToString(";")}")
}

fun checkSocket(host: String, port: Int) {
    Socket().use {
        try {
            it.connect(InetSocketAddress(host, port), 2000)
            it.close()
            log.debug("socket connect worked")
        } catch (e: Exception) {
            log.debug("looks like we can't connect?!")
            log.debug("${e.message}")
        }
    }
}
