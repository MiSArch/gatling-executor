package org.misarch.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class MainMetricsForwarderTask : DefaultTask() {
    @TaskAction
    fun mainForwardMetrics() {
        forwardMetrics(false)
    }
}