package org.misarch.common

import io.gatling.javaapi.core.OpenInjectionStep
import io.gatling.javaapi.core.ScenarioBuilder

abstract class BaseMiSArchLoadTest(
    val scenario: ScenarioBuilder,
    val openRampSteps: List<OpenInjectionStep>
)