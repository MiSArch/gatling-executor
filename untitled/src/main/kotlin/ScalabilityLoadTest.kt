package org.misarch

import io.gatling.javaapi.core.CoreDsl.*
import org.misarch.scenarios.buyProcessScenario


private val userSteps = listOf(10000)
private val openRampSteps = userSteps.map { rampUsers(it).during(60) }
class ScalabilityLoadTest : BaseMiSArchLoadTest(buyProcessScenario, openRampSteps)
