package com.mkring.wildlydeplyplugin

import org.jboss.`as`.cli.scriptsupport.CLI

fun main(args: Array<String>) {
    println("hello")

    val cli = CLI.newInstance()
    cli.connect("localhost", 10090, "jboss", "root1234".toCharArray())
    val result = cli.cmd(":read-attribute(name=server-state) ")
    val response = result.getResponse()
    val serverstate = response.get("result")
    println("Current server state: " + serverstate)

    val response1 = cli.cmd("deploy --force /path/to/war-1.0.0-SNAPSHOT.war")
            .response
    println(response1)

    cli.disconnect()
}