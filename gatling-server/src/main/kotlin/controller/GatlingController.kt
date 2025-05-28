package org.misarch.gatlingserver.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.misarch.gatlingserver.service.GatlingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

private val logger = KotlinLogging.logger {}

@RestController
class GatlingController(
    private val gatlingService: GatlingService
) {
    @PostMapping("/execute")
     fun getGatlingData(
        @RequestParam testUUID: UUID,
        @RequestParam accessToken: String,
        @RequestParam triggerDelay: Long,
        @RequestParam targetUrl: String,
        @RequestBody userSteps: String,

     ): ResponseEntity<Unit> {
        logger.info { "Received new execution run for test $testUUID" }
        gatlingService.executeGatlingTest(userSteps, testUUID, accessToken, triggerDelay, targetUrl)
        return ResponseEntity.accepted().body(Unit)
    }
}