package org.misarch

import io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class WarmUpSimulation : Simulation() {

    private val warmUpRate = System.getenv("WARM_UP_RATE") ?: throw IllegalStateException("Environment variable WARM_UP_RATE is not set")
    private val warmUpDuration = System.getenv("WARM_UP_DURATION") ?: throw IllegalStateException("Environment variable WARM_UP_DURATION is not set")

    private val httpProtocol = http.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")


    init {
        val scenarioSetups = scenarios.mapNotNull { (scenario, _) ->
            scenario.injectClosed(constantConcurrentUsers(warmUpRate.toInt()).during(warmUpDuration.toLong())).protocols(httpProtocol)
        }
        setUp(scenarioSetups)
    }
}