plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "compiler"

version = "1.0-SNAPSHOT"

val mainClassName = "MainKt"

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(20) }

application { mainClass = "compiler.MainKt" }

tasks.withType<Jar> {
    manifest { attributes("Main-Class" to "${project.group}.$mainClassName") }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
