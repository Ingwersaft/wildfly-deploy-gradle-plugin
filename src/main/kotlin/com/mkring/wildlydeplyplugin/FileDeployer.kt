package com.mkring.wildlydeplyplugin

import org.jboss.`as`.cli.CommandLineException
import org.jboss.`as`.cli.scriptsupport.CLI

class FileDeployer(val extension: DeployWildplyPluginExtension) {
    fun deploy() {
        with(extension) {
            CLI.newInstance().let { cli ->
                println("wildfly connect with $user on $host:$port")
                cli.connect(host, port, user, password?.toCharArray())
                val force = if (force) {
                    "--force"
                } else {
                    ""
                }
                println("connected successfully")
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
}