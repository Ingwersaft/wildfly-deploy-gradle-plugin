package com.mkring.wildlydeplyplugin

fun main(args: Array<String>) {
    println("hello")
    DeployWildflyTask(
            host = "host",
            port = 9090,
            user = "user",
            password = "pass",
            file = "filePath"
    ).deployWildfly()
}
