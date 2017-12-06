package com.mkring.wildlydeplyplugin

import org.jboss.`as`.cli.scriptsupport.CLI
import java.io.File

fun main(args: Array<String>) {
    println("hello")
    val file = File("/home/mauer/workspace/uploadreceiver/build/libs/UploadReceiver-1.0.0-SNAPSHOT.war")
    println(file.exists())

    val cli = CLI.newInstance()
    cli.connect("localhost", 10090, "jboss", "root1234".toCharArray())
    val result = cli.cmd(":read-attribute(name=server-state) ")
    val response = result.getResponse()
    val serverstate = response.get("result")

    println("Current server state: " + serverstate)

    cli.disconnect()
}