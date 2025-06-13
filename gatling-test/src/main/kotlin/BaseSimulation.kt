package org.misarch

import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

class BaseSimulation : Simulation() {

    private val token = System.getenv("ACCESS_TOKEN") ?: throw IllegalStateException("Environment variable ACCESS_TOKEN is not set")
    private val baseUrl = System.getenv("BASE_URL") ?: throw IllegalStateException("Environment variable BASE_URL is not set")
    private val testUUID = System.getenv("TEST_UUID") ?: throw IllegalStateException("Environment variable TEST_UUID is not set")
    private val testVersion = System.getenv("TEST_VERSION") ?: throw IllegalStateException("Environment variable TEST_VERSION is not set")
    private val experimentExecutorBaseUrl =
        System.getenv("EXPERIMENT_EXECUTOR_URL") ?: throw IllegalStateException("Environment variable EXPERIMENT_EXECUTOR_URL is not set")

    private val httpProtocol = http.baseUrl(baseUrl).authorizationHeader("Bearer $token").contentTypeHeader("application/json").doNotTrackHeader("1")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")


    override fun before() {
        waitForTrigger()
        super.before()
    }

    init {
        val scenarioSetups = scenarios.mapNotNull { (scenario, name) ->
            val userSteps = File("src/main/resources/$name").readLines().mapNotNull { it.trim().toIntOrNull() }
            val scaledSteps = userSteps.map { stepUsers -> rampUsers(stepUsers).during(1) }
            scenario.injectOpen(scaledSteps).protocols(httpProtocol)
        }
        setUp(scenarioSetups)
    }

    private fun waitForTrigger() {
        val maxAttempts = 6000
        val retryInterval = 100L

        // Register at experiment executor
        httpRequest("$experimentExecutorBaseUrl/trigger/$testUUID/$testVersion?client=gatling", "POST")

        println("Waiting for trigger...")

        for (i in 1..maxAttempts) {
            val triggerResponse = httpRequest("$experimentExecutorBaseUrl/trigger/$testUUID/$testVersion", "GET")
            if (triggerResponse == "true") {
                println("Trigger pulled successfully.")
                return
            }
            println("Waiting for trigger... Attempt $i / $maxAttempts")
            Thread.sleep(retryInterval)
        }
        throw RuntimeException("Trigger not pulled after 1000 attempts. Aborting Experiment.")
    }

    private fun httpRequest(url: String, method: String): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = method
        val status = connection.responseCode
        if (!(status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_ACCEPTED || status == HttpURLConnection.HTTP_NO_CONTENT)) {
            throw RuntimeException("Failed to connect to $url. Response code: $status")
        }
        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}