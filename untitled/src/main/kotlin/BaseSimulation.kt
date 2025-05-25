package org.misarch

import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.net.HttpURLConnection
import java.net.URI

class BaseSimulation : Simulation() {


    private val experimentExecutorBaseUrl = "http://192.168.178.155:8888"
    private val token = System.getenv("ACCESS_TOKEN") ?: throw IllegalStateException("Environment variable ACCESS_TOKEN is not set")
    private val baseUrl = System.getenv("BASE_URL") ?: throw IllegalStateException("Environment variable BASE_URL is not set")
    private val testUUID = System.getenv("TEST_UUID") ?: throw IllegalStateException("Environment variable TEST_UUID is not set")

    private val httpProtocol = http.baseUrl(baseUrl).authorizationHeader("Bearer $token").contentTypeHeader("application/json").doNotTrackHeader("1")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")


    override fun before() {
        waitForTrigger()
        super.before()
    }

    init {
        val testClassName = System.getenv("TEST_CLASS") ?: throw IllegalStateException("Environment variable TEST_CLASS is not set")
        println("testClassName = $testClassName")
        val simulation: BaseMiSArchLoadTest = when (testClassName) {
            "org.misarch.ScalabilityLoadTest" -> ScalabilityLoadTest()
            "org.misarch.ResilienceLoadTest" -> ResilienceLoadTest()
            "org.misarch.ElasticityLoadTest" -> ElasticityLoadTest()
            "org.misarch.RampUpListLoadTest" -> RampUpListLoadTest()
            else -> throw IllegalArgumentException("Unknown test class: $testClassName")
        }

        setUp(simulation.scenario.injectOpen(simulation.openRampSteps).protocols(httpProtocol))
    }

    private fun waitForTrigger() {
        val maxAttempts = 300
        val retryInterval = 100L

        println("Waiting for trigger...")
        for (i in 1..maxAttempts) {
            val triggerResponse = getHttpRequest("$experimentExecutorBaseUrl/trigger/$testUUID")
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
