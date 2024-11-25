plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.1.0-RC2"
}

group = "io.github.lunbun"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.brott.dev/")
}

dependencies {
    implementation("com.github.rh-robotics:MeepMeep:-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
}