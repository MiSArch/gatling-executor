import org.misarch.gradle.MetricsForwarderTask

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.gatling.gradle") version "3.13.5"
}

group = "org.misarch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.gatling:gatling-core:3.13.5")
    implementation("io.gatling:gatling-core-java:3.13.5")
    implementation("io.gatling:gatling-http-java:3.13.5")
    implementation("io.gatling:gatling-app:3.13.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.register<MetricsForwarderTask>("forwardMetrics") {
    mustRunAfter("gatlingRun")
}