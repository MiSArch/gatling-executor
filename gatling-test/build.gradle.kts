import org.misarch.gradle.MainMetricsForwarderTask
import org.misarch.gradle.SteadyStateMetricsForwarderTask

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

tasks.register("warmUp") {
    group = "gatling"
    dependsOn("compileGatlingKotlin")
    doLast {
        javaexec {
            mainClass.set("io.gatling.app.Gatling")
            classpath = sourceSets["gatling"].runtimeClasspath
            args = listOf(
                "-s", "org.misarch.WarmUpSimulation",
                "-rf", "build/reports/gatling/warmup",
                "-la", "gradle",
                "-bf", GradleVersion.current().version
            )
            jvmArgs = listOf(
                "-Dfile.encoding=UTF-8",
                "--add-opens", "java.base/java.lang=ALL-UNNAMED"
            )
        }
    }
}

tasks.register("steadyState") {
    group = "gatling"
    dependsOn("compileGatlingKotlin")
    doLast {
        javaexec {
            mainClass.set("io.gatling.app.Gatling")
            classpath = sourceSets["gatling"].runtimeClasspath
            args = listOf(
                "-s", "org.misarch.SteadyStateSimulation",
                "-rf", "build/reports/gatling/steadystate",
                "-la", "gradle",
                "-bf", GradleVersion.current().version
            )
            jvmArgs = listOf(
                "-Dfile.encoding=UTF-8",
                "--add-opens", "java.base/java.lang=ALL-UNNAMED"
            )
        }
    }
}

tasks.register<SteadyStateMetricsForwarderTask>("steadyStateForwardMetrics") {
    mustRunAfter("steadyState")
}

tasks.register("mainSimulation") {
    group = "gatling"
    dependsOn("compileGatlingKotlin")
    doLast {
        javaexec {
            mainClass.set("io.gatling.app.Gatling")
            classpath = sourceSets["gatling"].runtimeClasspath
            args = listOf(
                "-s", "org.misarch.MainSimulation",
                "-rf", "build/reports/gatling/main",
                "-la", "gradle",
                "-bf", GradleVersion.current().version
            )
            jvmArgs = listOf(
                "-Dfile.encoding=UTF-8",
                "--add-opens", "java.base/java.lang=ALL-UNNAMED"
            )
        }
    }
}

tasks.register<MainMetricsForwarderTask>("mainForwardMetrics") {
    mustRunAfter("mainSimulation")
}