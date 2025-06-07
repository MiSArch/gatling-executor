package org.misarch.gatlingserver.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.misarch.gatlingserver.service.GatlingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

private val logger = KotlinLogging.logger {}

@RestController
class GatlingController(
    private val gatlingService: GatlingService
) {
    // TODO
    @PostMapping("/add-scenario")
    fun addScenario(
        @RequestParam testUUID: UUID,
        @RequestParam testVersion: String,
    ): ResponseEntity<Unit> {
        logger.info { "Received new scenario for test $testUUID and version $testVersion" }
        return ResponseEntity.accepted().body(Unit)
    }

    @PostMapping("/start-experiment")
     fun getGatlingData(
        @RequestParam testUUID: UUID,
        @RequestParam testVersion: String,
        @RequestParam accessToken: String,
        @RequestParam targetUrl: String,
        @RequestBody userSteps: String,

     ): ResponseEntity<Unit> {
        logger.info { "Received new execution run for test $testUUID and version $testVersion" }
        gatlingService.executeGatlingTest(userSteps, testUUID, testVersion, accessToken, targetUrl)
        return ResponseEntity.accepted().body(Unit)
    }

    @PostMapping("/stop-experiment")
    fun stopExperiment(@RequestParam testUUID: UUID, @RequestParam testVersion: String): ResponseEntity<String> {
        logger.info { "Attempting to stop experiment with testUUID: $testUUID" }
        return if (gatlingService.stopExperiment(testUUID, testVersion)) {
            ResponseEntity.ok("Experiment $testUUID stopped successfully.")
        } else {
            ResponseEntity.notFound().build()
        }
    }
}