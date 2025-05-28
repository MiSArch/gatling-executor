plugins {
    kotlin("jvm") version "2.1.10"
    id("io.gatling.gradle") version "3.13.5"
}

group = "org.misarch.gradle"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(gradleApi())
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}