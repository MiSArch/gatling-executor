package org.misarch

import io.gatling.javaapi.core.OpenInjectionStep
import io.gatling.javaapi.core.ScenarioBuilder

open class BaseMiSArchLoadTest(
    val scenario: ScenarioBuilder,
    val openRampSteps: List<OpenInjectionStep>
)