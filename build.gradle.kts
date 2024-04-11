plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "compiler"

version = "1.0-SNAPSHOT"

val mainClassName = "compiler.MainKt"

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(20) }

application { mainClass = "$mainClassName" }

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest { attributes("Main-Class" to "$mainClassName") }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
