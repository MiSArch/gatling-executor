package org.misarch.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class SteadyStateMetricsForwarderTask : DefaultTask() {
    @TaskAction
    fun steadyStateForwardMetrics() {
        forwardMetrics(true)
    }
}