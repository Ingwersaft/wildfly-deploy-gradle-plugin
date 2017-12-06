package com.mkring.wildlydeplyplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.invoke

open class DeployWildflyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            tasks {
                "myCopyTask"(Copy::class) {
                    from("build.gradle.kts")
                    into("build/copy")
                }
            }
        }
    }

}