package org.misarch.gatlingserver.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.UUID

@Service
class GatlingService(
    @Value("\${experiment-executor.url}") private val experimentExecutorUrl: String,
) {
    fun executeGatlingTest(userSteps: String, testUUID: UUID, accessToken: String, triggerDelay: Long, targetUrl: String) {
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
        processBuilder.environment()["TRIGGER_DELAY"] = triggerDelay.toString()
        processBuilder.environment()["BASE_URL"] = targetUrl
        processBuilder.environment()["TEST_UUID"] = testUUID.toString()

        processBuilder.start()
    }

}