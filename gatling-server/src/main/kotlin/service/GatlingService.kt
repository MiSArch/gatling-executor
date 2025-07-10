package org.misarch.gatlingserver.service

import org.misarch.gatlingserver.controller.model.EncodedFileDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Service
@OptIn(ExperimentalEncodingApi::class)
class GatlingService(
    @Value("\${experiment-executor.url}") private val experimentExecutorUrl: String,
) {
    private val runningProcesses = ConcurrentHashMap<String, Process>()

    fun executeGatlingTest(
        gatlingConfigs: List<EncodedFileDTO>,
        testUUID: UUID,
        testVersion: String,
        warmUp: Boolean,
        warmUpRate: Int,
        warmUpDuration: Int,
        steadyState: Boolean,
        steadyStateRate: Int,
        steadyStateDuration: Int,
    ) {

        gatlingConfigs.forEach { config ->
            val decodedWorkContent = Base64.decode(config.encodedWorkFileContent).decodeToString()
            val decodedUserStepsContent = Base64.decode(config.encodedUserStepsFileContent).decodeToString()
            File("/gatling/src/main/kotlin/${config.fileName}.kt").writeText(decodedWorkContent)
            File("/gatling/src/main/resources/${config.fileName}.csv").writeText(decodedUserStepsContent)
        }

        val scenarios = gatlingConfigs.joinToString(",\n") { config ->
            "${config.fileName} to \"${config.fileName}.csv\""
        }

        File("/gatling/src/main/kotlin/Scenarios.kt").writeText("""
            package org.misarch

            val scenarios = mapOf(
                $scenarios
            )
            """.trimIndent()
        )

        val gradleString = when {
            warmUp && steadyState -> "warmUp steadyState steadyStateForwardMetrics mainSimulation mainForwardMetrics"
            warmUp -> "warmUp mainSimulation mainForwardMetrics"
            steadyState -> "steadyState steadyStateForwardMetrics mainSimulation mainForwardMetrics"
            else -> "mainSimulation mainForwardMetrics"
        }

        val processBuilder = ProcessBuilder("bash", "-c", "/gatling/gradlew $gradleString")
            .directory(File("/gatling"))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)

        processBuilder.environment()["EXPERIMENT_EXECUTOR_URL"] = experimentExecutorUrl
        processBuilder.environment()["TEST_UUID"] = testUUID.toString()
        processBuilder.environment()["TEST_VERSION"] = testVersion
        processBuilder.environment()["WARM_UP_RATE"] = warmUpRate.toString()
        processBuilder.environment()["WARM_UP_DURATION"] = warmUpDuration.toString()
        processBuilder.environment()["STEADY_STATE_RATE"] = steadyStateRate.toString()
        processBuilder.environment()["STEADY_STATE_DURATION"] = steadyStateDuration.toString()

        val process = processBuilder.start()
        runningProcesses["$testUUID:$testVersion"] = process
    }

    fun stopExperiment(testUUID: UUID, testVersion: String): Boolean {
        val process = runningProcesses.remove("$testUUID:$testVersion")
        return if (process != null) {
            process.destroy()
            true
        } else {
            false
        }
    }
}