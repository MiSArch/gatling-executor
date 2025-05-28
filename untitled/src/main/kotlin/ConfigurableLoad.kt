package org.misarch

import io.gatling.javaapi.core.CoreDsl.*
import java.io.File

class ConfigurableLoad {

    private val userSteps = readUserStepsCsv()
    val scenario = defaultScenario
    val openRampSteps = userSteps.map { rampUsers(it).during(1) }

    private fun readUserStepsCsv(): List<Int> {
        return File("src/main/resources/gatling-usersteps.csv").readLines().mapNotNull { it.trim().toIntOrNull() }
    }
}