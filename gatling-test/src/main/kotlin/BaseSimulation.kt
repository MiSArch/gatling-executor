package org.misarch

import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.net.HttpURLConnection
import java.net.URI

class BaseSimulation : Simulation() {

    private val token = System.getenv("ACCESS_TOKEN") ?: throw IllegalStateException("Environment variable ACCESS_TOKEN is not set")
    private val baseUrl = System.getenv("BASE_URL") ?: throw IllegalStateException("Environment variable BASE_URL is not set")
    private val testUUID = System.getenv("TEST_UUID") ?: throw IllegalStateException("Environment variable TEST_UUID is not set")
    private val testVersion = System.getenv("TEST_VERSION") ?: throw IllegalStateException("Environment variable TEST_VERSION is not set")
    private val trigger = System.getenv("TRIGGER_DELAY")?.toLong() ?: throw IllegalStateException("Environment variable TRIGGER_DELAY is not set")
    private val experimentExecutorBaseUrl =
        System.getenv("EXPERIMENT_EXECUTOR_URL") ?: throw IllegalStateException("Environment variable EXPERIMENT_EXECUTOR_URL is not set")

    private val httpProtocol = http.baseUrl(baseUrl).authorizationHeader("Bearer $token").contentTypeHeader("application/json").doNotTrackHeader("1")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")


    override fun before() {
        waitForTrigger()
        super.before()
    }

    init {
        val simulation = ConfigurableLoad()
        setUp(simulation.scenario.injectOpen(simulation.openRampSteps).protocols(httpProtocol))
    }

    private fun waitForTrigger() {
        val maxAttempts = trigger / 100
        val retryInterval = 100L

        println("Waiting for trigger...")
        for (i in 1..maxAttempts) {
            val triggerResponse = getHttpRequest("$experimentExecutorBaseUrl/trigger/$testUUID/$testVersion")
            if (triggerResponse == "true") {
                println("Trigger pulled successfully.")
                return
            }
            println("Waiting for trigger... Attempt $i / $maxAttempts")
            Thread.sleep(retryInterval)
        }
    }

    private fun getHttpRequest(url: String): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw RuntimeException("Failed to connect to $url. Response code: $responseCode")
        }
        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}

// TODO fuzzing?

// Open Injection Patterns
// rampUsers(100).during(Duration.ofSeconds(30))
// constantUsersPerSec(10.0).during(Duration.ofSeconds(60))
// rampUsersPerSec(1.0).to(10.0).during(Duration.ofSeconds(30))
// stressPeakUsers(200)
// heavisideUsers(100).during(Duration.ofSeconds(20)

// Closed Injection Patterns
// constantConcurrentUsers(50).during(Duration.ofMinutes(5))
// rampConcurrentUsers(10).to(100).during(Duration.ofMinutes(10))

// Combination
// setUp(
//    buyProcessScenario.injectOpen(
//        atOnceUsers(10),
//        rampUsers(50).during(Duration.ofSeconds(30)),
//        constantUsersPerSec(20.0).during(Duration.ofMinutes(2))
//    ).protocols(httpProtocol)
//)