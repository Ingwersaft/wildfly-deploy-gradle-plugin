package com.mkring.wildlydeplyplugin

fun main(args: Array<String>) {
    println("hello")
    CliExecutioner.execute(
        host = "127.0.0.1",
        password = "123",
        port = 9990,
        user = "testuser",
        commands = listOf("deployment-info")
    )
//    DeployWildflyTask().apply {
//        host = "host"
//        port = 9090
//        user = "user"
//        password = "pass"
//        file = "filePath"
//    }.deployWildfly()
}
