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

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create<MavenPublication>("pedromeep") {
            groupId = "io.github.lunbun"
            artifactId = "pedromeep"
            version = "1.0.0"

            from(components["java"])
            artifact(sourcesJar)

            pom {
                packaging = "jar"
                name.set(rootProject.name)
            }
        }
    }

    repositories {
        maven {
            url = uri("${layout.buildDirectory}/repository")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}