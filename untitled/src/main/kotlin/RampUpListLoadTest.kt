package org.misarch

import io.gatling.javaapi.core.CoreDsl.*
import java.io.File
import org.misarch.scenarios.buyProcessScenario

class RampUpListLoadTest : BaseMiSArchLoadTest(buyProcessScenario, openRampSteps)

private val userSteps = readUserStepsCsv("src/main/resources/userstepsshort.csv")
private val openRampSteps = userSteps.map { rampUsers(it).during(1) }

fun readUserStepsCsv(filePath: String): List<Int> {
    return File(filePath).readLines().mapNotNull { it.trim().toIntOrNull() }
}