package org.misarch.tests

import io.gatling.javaapi.core.CoreDsl.*
import org.misarch.common.BaseMiSArchLoadTest
import java.io.File
import org.misarch.scenarios.defaultScenario

class RampUpListLoadTest : BaseMiSArchLoadTest(defaultScenario, openRampSteps)

private val userSteps = readUserStepsCsv("src/main/resources/gatling-usersteps.csv")
private val openRampSteps = userSteps.map { rampUsers(it).during(1) }

fun readUserStepsCsv(filePath: String): List<Int> {
    return File(filePath).readLines().mapNotNull { it.trim().toIntOrNull() }
}