package org.misarch.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI

open class MetricsForwarderTask : DefaultTask() {
    private val testUUID = System.getenv("TEST_UUID")

    @TaskAction
    fun forwardMetrics() {
        println("Running forwardMetrics...")
        val reportDir = File("build/reports/gatling")
        val maxRetries = 30
        val retryInterval = 1000L

        var latest: String? = null
        for (i in 1..maxRetries) {
            latest = reportDir.listFiles()
                ?.filter { it.isDirectory }
                ?.maxOfOrNull { it.name }

            if (latest != null) {
                val statsFile = File("build/reports/gatling/$latest/js/stats.js")
                val htmlFile = File("build/reports/gatling/$latest/index.html")
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

        val experimentExecutorBaseUrl =
            System.getenv("EXPERIMENT_EXECUTOR_URL") ?: throw IllegalStateException("Environment variable EXPERIMENT_EXECUTOR_URL is not set")
        val rawJs = File("build/reports/gatling/$latest/js/stats.js").readText()
        val rawHtml = File("build/reports/gatling/$latest/index.html").readText()
        val jsUrl = "$experimentExecutorBaseUrl/experiment/$testUUID/gatling/metrics/stats"
        val htmlUrl = "$experimentExecutorBaseUrl/experiment/$testUUID/gatling/metrics/html"

        sendHttpRequest(jsUrl, rawJs)
        sendHttpRequest(htmlUrl, rawHtml)
        println("Metrics forwarded successfully.")
    }
}

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