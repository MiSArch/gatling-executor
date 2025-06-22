package org.misarch.gatlingserver.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.misarch.gatlingserver.service.GatlingService
import org.misarch.gatlingserver.controller.model.EncodedFileDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

private val logger = KotlinLogging.logger {}

@RestController
class GatlingController(
    private val gatlingService: GatlingService
) {
    @PostMapping("/start-experiment")
     fun getGatlingData(
        @RequestParam testUUID: UUID,
        @RequestParam testVersion: String,
        @RequestBody gatlingConfigs: List<EncodedFileDTO>,

     ): ResponseEntity<Unit> {
        logger.info { "Received new execution run for test $testUUID and version $testVersion" }
        gatlingService.executeGatlingTest(gatlingConfigs, testUUID, testVersion)
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