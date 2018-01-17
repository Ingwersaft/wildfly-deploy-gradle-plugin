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

class FileDeployer(val file: String?, val host: String, val port: Int, val user: String?, val password: String?,
                   val reload: Boolean, val force: Boolean, val name: String?, val runtimeName: String?, val awaitReload: Boolean) {
    fun deploy() {
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
            println("given $file existent: ${File(file).isFile}")
            val deploySuccess = cli.cmd("deploy $force $name $runtimeName $file").isSuccess
            println("deploy success: $deploySuccess")
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
            val postReloadDeploymentInfoPrettyPrint = blockingCmd("deployment-info", 2, ChronoUnit.MINUTES).response.responsePrettyPrint()
            println("POST reload deployment info:\n$postReloadDeploymentInfoPrettyPrint")
        }

    }

    private fun connect(cli: CLI) {
        cli.connect(host, port, user, password?.toCharArray())
    }

    private fun awaitReload(cli: CLI) {
        try {
            val deploymentInfoResponseText = cli.cmd("deployment-info").response.asString()
            println("deployment info after reload:\n$deploymentInfoResponseText")
        } catch (e: CommandLineException) {
            println("looks like reload timed out: ${e.message}")
        }
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
                println("trying")
                val cmd = cli.cmd(s)
                if (cmd.isSuccess.not()) {
                    throw IllegalStateException("no success")
                }
                return cmd
            } catch (e: Exception) {
                System.err.println(e.message)
                Thread.sleep(500)
                continue
            } finally {
                try {
                    cli.disconnect()
                } catch (e: Exception) {
                }
            }
        }
        throw IllegalStateException("cant reconnect after reload after $i $unit")
    }

    private fun ModelNode.responsePrettyPrint() = get("result").asList().joinToString("\n") {
        "${it.asProperty().name}: " +
                "${it.getParam("enabled")}; " +
                "${it.getParam("runtime-name")}; " +
                "${it.getParam("status")}; " +
                it.getParam("enabled-timestamp")
    }

    private fun ModelNode.getParam(s: String) = "$s: ${get(0).get(s)}"
}