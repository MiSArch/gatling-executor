package org.misarch

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.OpenInjectionStep
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.http.HttpDsl.http
import java.time.Duration

open class BaseMiSArchLoadTest(
    val scenario: ScenarioBuilder,
    val openRampSteps: List<OpenInjectionStep>
) {

    private val token = System.getenv("ACCESS_TOKEN") ?: throw IllegalStateException("Environment variable ACCESS_TOKEN is not set")
    private val baseUrl = System.getenv("BASE_URL") ?: throw IllegalStateException("Environment variable BASE_URL is not set")
    private val testUUID = System.getenv("TEST_UUID") ?: throw IllegalStateException("Environment variable TEST_UUID is not set")

    val httpProtocol = http.baseUrl(baseUrl).authorizationHeader("Bearer $token").contentTypeHeader("application/json").doNotTrackHeader("1")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

    val waitForTriggerScenario = scenario("Wait for Trigger").asLongAsDuring({ session ->
        session.getString("triggerResponse") != "true"
    }, Duration.ofSeconds(30)) // Timeout after 30s
        .on(
            exec(
                http("Check Trigger").get("http://localhost:8888/trigger/$testUUID").check(bodyString().saveAs("triggerResponse"))
            ).pause(Duration.ofMillis(100))
        )
}