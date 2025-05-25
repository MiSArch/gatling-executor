package org.misarch.tests

import io.gatling.javaapi.core.CoreDsl.*
import org.misarch.common.BaseMiSArchLoadTest
import org.misarch.scenarios.defaultScenario


private val userSteps = listOf(1000,1000,1000,1000)
private val openRampSteps = userSteps.map {
    rampUsers(it).during(30)
    atOnceUsers(it)
}
class ResilienceLoadTest : BaseMiSArchLoadTest(defaultScenario, openRampSteps)
