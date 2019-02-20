package com.mkring.wildlydeplyplugin

import org.jboss.`as`.cli.scriptsupport.CLI
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(CliExecutioner::class.java)

object CliExecutioner {
    fun execute(
        host: String,
        port: Int,
        user: String?,
        password: String?,
        commands: List<String>
    ) {
        log.debug("deploy(): " + this)
        checkHostDns(host)
        checkSocket(host, port)
        CLI.newInstance().let { cli ->
            log.debug("wildfly connect with $user on $host:$port")
            connect(cli, host, port, user, password)

            commands.forEach {
                log.debug("going to execute `$it`")
                val result: CLI.Result? = cli.cmd(it)
                log.debug("result: ${result?.isSuccess}")
                try {
                    result?.response?.get("result")?.asString()?.let {
                        log.debug("result string:\n$it")
                    } ?: run { println("result or response null") }
                } catch (e: Exception) {
                    log.debug("cmd might have failed: ${e.message}")
                }
            }

            cli.disconnect()
        }
    }
}