package com.mkring.wildlydeplyplugin

import org.jboss.`as`.cli.CommandLineException
import org.jboss.`as`.cli.scriptsupport.CLI
import org.jboss.dmr.ModelNode
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class FileDeployer(
    val file: String?, val host: String, val port: Int, val user: String?, val password: String?,
    val reload: Boolean, val force: Boolean, val name: String?, val runtimeName: String?, val awaitReload: Boolean,
    val undeployBeforehand: Boolean
) {
    fun deploy() {
        println("deploy(): " + this)
        checkHostDns()
        checkSocket()
        CLI.newInstance().let { cli ->
            println("wildfly connect with $user on $host:$port")
            connect(cli)
            val force = if (force) {
                "--force"
            } else {
                ""
            }
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
            println("connected successfully")
            val deploymentExists = File(file).isFile
            println("given $file existent: $deploymentExists")
            if (deploymentExists.not()) throw IllegalStateException("couldn't find given deployment")

            if (undeployBeforehand) {
                println("\nundeploying existing deployment with same name if preset...")
                val shouldUndeploy =
                    blockingCmd("deployment-info", 2, ChronoUnit.MINUTES).response.get("result").asList()
                        .any { it.asProperty().name == name.removePrefix("--name=") }
                println("shouldUndeploy=$shouldUndeploy")
                if (shouldUndeploy) {
                    blockingCmd("undeploy $name", 2, ChronoUnit.MINUTES).response.also {
                        println("undeploy response: $it\n")
                    }
                }
            }

            // deploy
            val deploySuccess = cli.cmd("deploy $force $name $runtimeName $file").isSuccess
            println("deploy success: $deploySuccess")

            enableDeploymentIfNecessary(name)

            if (reload) {
                try {
                    val reloadSuccess = cli.cmd("reload").isSuccess
                    println("reload success: $reloadSuccess")
                } catch (e: CommandLineException) {
                    println("looks like reload timed out: ${e.message}")
                }
            }
            cli.disconnect()
        }

        if (awaitReload) {
            println("going to block until the reload finished...\n")
            Thread.sleep(1000)
            val postReloadDeploymentInfoPrettyPrint =
                blockingCmd("deployment-info", 1, ChronoUnit.MINUTES).response.responsePrettyPrint()
            println("\n\nPOST reload deployment info:\n$postReloadDeploymentInfoPrettyPrint")
        }

    }

    private fun enableDeploymentIfNecessary(name: String) {
        println("\nchecking if deployment is enabled...")
        val deploymentEnabled =
            blockingCmd("deployment-info", 2, ChronoUnit.MINUTES).response.get("result").asList().map {
                it.asProperty().name to it.getParam("enabled").removePrefix("enabled: ")
            }.firstOrNull {
                it.first == name.removePrefix("--name=")
            }?.second?.toBoolean()

        if (deploymentEnabled == false) {
            println("not enabled! going to enable now!")
            blockingCmd("deploy $name", 2, ChronoUnit.MINUTES).response.also {
                println("enable response: $it\n")
            }
        }
    }

    private fun connect(cli: CLI) {
        cli.connect(host, port, user, password?.toCharArray())
    }

    private fun checkHostDns() {
        println("$host DNS: ${InetAddress.getAllByName(host).joinToString(";")}")
    }

    private fun checkSocket() {
        Socket().use {
            try {
                it.connect(InetSocketAddress(host, port), 2000)
                it.close()
                println("socket connect worked")
            } catch (e: Exception) {
                println("looks like we can't connect?!")
                println("${e.message}")
                e.printStackTrace()
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
                connect(cli)
                val cmd = cli.cmd(s)
                if (cmd.isSuccess.not()) {
                    throw IllegalStateException("no success")
                }
                return cmd
            } catch (e: Exception) {
                println("connect + cmd exception: ${e::class.java.simpleName}:${e.message}")
                Thread.sleep(500)
                continue
            } finally {
                try {
                    cli.disconnect()
                } catch (e: Exception) {
                    println("disconnect exception: ${e::class.java.simpleName}:${e.message}")
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
        return "FileDeployer(file=$file, host='$host', port=$port, user=$user, reload=$reload, force=$force, name=$name, runtimeName=$runtimeName, awaitReload=$awaitReload)"
    }


}