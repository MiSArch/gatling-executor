package org.misarch

import io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class SteadyStateSimulation : Simulation() {

    private val steadyStateRate = System.getenv("STEADY_STATE_RATE") ?: throw IllegalStateException("Environment variable STEADY_STATE_RATE is not set")
    private val steadyStateDuration = System.getenv("STEADY_STATE_DURATION") ?: throw IllegalStateException("Environment variable STEADY_STATE_DURATION is not set")

    private val httpProtocol = http.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

    init {
        val scenarioSetups = scenarios.mapNotNull { (scenario, _) ->
            scenario.injectClosed(constantConcurrentUsers(steadyStateRate.toInt()).during(steadyStateDuration.toLong())).protocols(httpProtocol)
        }
        setUp(scenarioSetups)
    }
}