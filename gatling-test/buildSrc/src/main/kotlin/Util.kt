package org.misarch.gradle

import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI

fun sendHttpRequest(url: String, data: String) {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "plain/text")
    connection.doOutput = true
    connection.outputStream.use { outputStream: OutputStream ->
        outputStream.write(data.toByteArray(Charsets.UTF_8))
    }

    val responseCode = connection.responseCode
    if (responseCode != HttpURLConnection.HTTP_OK) {
        throw RuntimeException("Failed to connect to $url. Response code: $responseCode")
    }
}

fun forwardMetrics(isSteadyState: Boolean) {
    println("Running forwardMetrics...")
    val reportDirPath = if (isSteadyState) {
        "build/reports/gatling/steadystate"
    } else {
        "build/reports/gatling/main"
    }

    val reportDir = File(reportDirPath)
    val maxRetries = 30
    val retryInterval = 1000L

    var latest: String? = null
    for (i in 1..maxRetries) {
        latest = reportDir.listFiles()
            ?.filter { it.isDirectory }
            ?.maxOfOrNull { it.name }

        if (latest != null) {
            val statsFile = File("$reportDirPath/$latest/js/stats.js")
            val htmlFile = File("$reportDirPath/$latest/index.html")
            if (statsFile.exists() && htmlFile.exists()) {
                println("Report files are ready.")
                break
            }
        }

        println("Waiting for report files... Attempt $i")
        Thread.sleep(retryInterval)
    }

    if (latest == null) {
        throw RuntimeException("Failed to find the latest report directory after $maxRetries attempts.")
    }

    val experimentExecutorBaseUrl = System.getenv("EXPERIMENT_EXECUTOR_URL")
    val testUUID = System.getenv("TEST_UUID")
    val testVersion = System.getenv("TEST_VERSION")

    if (experimentExecutorBaseUrl.isNullOrEmpty() || testUUID.isNullOrEmpty() || testVersion.isNullOrEmpty()) {
        throw RuntimeException("Environment variables EXPERIMENT_EXECUTOR_URL, TEST_UUID, or TEST_VERSION are not set.")
    }

    val rawJs = File("$reportDirPath/$latest/js/stats.js").readText()
    val rawHtml = File("$reportDirPath/$latest/index.html").readText()
    val url = if (isSteadyState) {
        "$experimentExecutorBaseUrl/experiment/$testUUID/$testVersion/gatling/metrics/steadyState"
    } else {
        "$experimentExecutorBaseUrl/experiment/$testUUID/$testVersion/gatling/metrics"
    }

    sendHttpRequest(url, "$rawHtml\nSPLIT_HERE\n$rawJs")
    println("Metrics forwarded successfully.")
}