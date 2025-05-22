package org.misarch

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.Simulation

class MainSimulation : Simulation() {
    private val simulation = RampUpListLoadTest()
    init {
        setUp(
            simulation.waitForTriggerScenario.injectOpen(atOnceUsers(1)).andThen(
                simulation.scenario.injectOpen(simulation.openRampSteps).protocols(simulation.httpProtocol)
            )
        )
    }
}

fun main() {
    val test = MainSimulation()
    test.run {}
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