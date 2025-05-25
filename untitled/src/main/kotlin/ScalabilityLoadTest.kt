package org.misarch

import io.gatling.javaapi.core.CoreDsl.*
import org.misarch.scenarios.defaultScenario


private val openRampSteps = listOf(rampUsersPerSec(1.0).to(16.67).during(60))
class ScalabilityLoadTest : BaseMiSArchLoadTest(defaultScenario, openRampSteps)
