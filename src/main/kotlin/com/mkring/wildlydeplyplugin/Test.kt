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

    val deploySuccess = cli.cmd("deploy --force /home/mauer/workspace/uploadreceiver/build/libs/UploadReceiver-1.0.0-SNAPSHOT.war").isSuccess
    println("deploySuccess=$deploySuccess")
//    println("info:=" + cli.cmd("deployment-info").response + " infoOUT")

    cli.disconnect()
}
