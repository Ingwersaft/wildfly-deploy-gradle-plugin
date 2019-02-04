package com.mkring.wildlydeplyplugin

import org.jboss.`as`.cli.scriptsupport.CLI

object CliExecutioner {
    fun execute(
        host: String,
        port: Int,
        user: String?,
        password: String?,
        commands: List<String>
    ) {
        println("deploy(): " + this)
        checkHostDns(host)
        checkSocket(host, port)
        CLI.newInstance().let { cli ->
            println("wildfly connect with $user on $host:$port")
            connect(cli, host, port, user, password)

            commands.forEach {
                println("going to execute `$it`")
                val result: CLI.Result? = cli.cmd(it)
                println("result: ${result?.isSuccess}")
                try {
                    result?.response?.get("result")?.asString()?.let {
                        println("result string:\n$it")
                    } ?: run { println("result or response null") }
                } catch (e: Exception) {
                    println("cmd might have failed: ${e.message}")
                }
            }

            cli.disconnect()
        }
    }
}