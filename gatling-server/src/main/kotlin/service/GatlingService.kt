package org.misarch.gatlingserver.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class GatlingService(
    @Value("\${experiment-executor.url}") private val experimentExecutorUrl: String,
) {
    private val runningProcesses = ConcurrentHashMap<String, Process>()


    fun addScenario(fileName: String, workFile: String) {
        storeWorkFile(fileName, workFile)
        // TODO read and update Scenarios.kt file with new scenario
    }

    fun removeScenario(fileName: String) {
        val file = File("/gatling/src/main/kotlin/$fileName.kt")
        if (file.exists()) {
            file.delete()
        } else {
            throw IllegalArgumentException("Scenario file $fileName does not exist.")
        }
        // TODO read and update Scenarios.kt file to remove scenario
    }

    private fun storeWorkFile(fileName: String, workFile: String) {
        val file = File("/gatling/src/main/kotlin/$fileName.kt")
        file.parentFile.mkdirs() // Ensure the directory exists
        file.writeText(workFile)
    }


    fun executeGatlingTest(userSteps: String, testUUID: UUID, testVersion: String, accessToken: String, targetUrl: String) {
        File("/gatling/src/main/resources/gatling-usersteps.csv").writeText(userSteps)
        val processBuilder = ProcessBuilder(
            "bash", "-c",
            "/gatling/gradlew gatlingRun forwardMetrics"
        )
            .directory(File("/gatling"))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)

        processBuilder.environment()["EXPERIMENT_EXECUTOR_URL"] = experimentExecutorUrl
        processBuilder.environment()["ACCESS_TOKEN"] = accessToken
        processBuilder.environment()["BASE_URL"] = targetUrl
        processBuilder.environment()["TEST_UUID"] = testUUID.toString()
        processBuilder.environment()["TEST_VERSION"] = testVersion

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