package com.mkring.wildlydeplyplugin

import org.jboss.`as`.cli.CommandLineException
import org.jboss.`as`.cli.scriptsupport.CLI
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class FileDeployer(val extension: DeployWildplyPluginExtension) {
    fun deploy() {
        with(extension) {
            checkHostDns()
            checkSocket()
            CLI.newInstance().let { cli ->
                println("wildfly connect with $user on $host:$port")
                cli.connect(host, port, user, password?.toCharArray())
                val force = if (force) {
                    "--force"
                } else {
                    ""
                }
                println("connected successfully")
                println("given $file existent: ${File(file).isFile}")
                val deploySuccess = cli.cmd("deploy $force $file").isSuccess
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
        }
    }

    private fun DeployWildplyPluginExtension.checkHostDns() {
        println("$host DNS: ${InetAddress.getAllByName(host).joinToString(";")}")
    }

    private fun DeployWildplyPluginExtension.checkSocket() {
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
}