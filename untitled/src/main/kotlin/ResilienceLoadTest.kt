package org.misarch

import io.gatling.javaapi.core.CoreDsl.*
import org.misarch.scenarios.buyProcessScenario


private val userSteps = listOf(1000,1000,1000,1000)
private val openRampSteps = userSteps.map {
    rampUsers(it).during(30)
    atOnceUsers(it)
}
class ResilienceLoadTest : BaseMiSArchLoadTest(buyProcessScenario, openRampSteps)
