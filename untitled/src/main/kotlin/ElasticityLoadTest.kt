package org.misarch

import io.gatling.javaapi.core.CoreDsl.*
import org.misarch.scenarios.defaultScenario


private val userSteps = listOf(3000,100,4000,100,5000,100,6000,100,7000)
private val openRampSteps = userSteps.map { rampUsers(it).during(60) }
class ElasticityLoadTest : BaseMiSArchLoadTest(defaultScenario, openRampSteps)