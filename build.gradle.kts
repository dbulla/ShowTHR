import org.gradle.internal.jvm.Jvm

//val ktorVersion = "3.3.0"
//val kotlinTestUnit = "1.9.10"

plugins {
    kotlin("jvm") version "2.4.0-Beta1"
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id( "com.gradleup.shadow") version "9.4.1"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("com.dorongold.task-tree") version "4.0.1"
    kotlin("plugin.serialization") version "2.3.20"
//    id("io.ktor.plugin") version "3.3.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation("javax.vecmath:vecmath:1.5.2")
//    implementation("me.saharnooby:qoi-java:1.2.1")
//    implementation("org.sejda.imageio:webp-imageio:0.1.6")
    implementation("com.google.guava:guava:33.5.0-jre")
    implementation("commons-io:commons-io:2.20.0")
    implementation(project.dependencies.enforcedPlatform("io.ktor:ktor-bom:3.3.1"))

    // client/server stuff
    implementation("ch.qos.logback:logback-classic:1.5.19")
//    implementation("io.ktor:ktor-server-call-logging")
//    implementation("io.ktor:ktor-server-content-negotiation")
//    implementation("io.ktor:ktor-server-default-headers" )
//    implementation("io.ktor:ktor-server-html-builder")
//    implementation("io.ktor:ktor-server-netty")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "com.nurflugel"
version = "1.0.0"

application {
    mainClass = "com.nurflugel.showthr.ShowTHR"
    group = "com.nurflugel"
    version = "1.1.1"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.register<JavaExec>("runRenamer") {
    group = "application"
    description = "Runs Renamer"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.nurflugel.showthr.Renamer"
}

tasks.register<JavaExec>("runAnalyzeOutput") {
    group = "application"
    description = "Runs AnalyzeOutput"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.nurflugel.showthr.AnalyzeOutput"
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Runs ShowTHR Client"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.nurflugel.showthr.Client"
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Runs ShowTHR Server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.nurflugel.showthr.ServerKt"
    args = listOf<String>()
}

println("""
             ========================================================================================================
             Welcome to Gradle version:          ${project.gradle.gradleVersion}
             Java version:                       ${Jvm.current()}
             Java home:                          ${Jvm.current().javaHome}
             Gradle user directory is set to:    ${project.gradle.gradleUserHomeDir}
             Project directory:                  ${project.projectDir}
             Running build script:               ${project.buildFile}
             Subprojects:                        ${project.subprojects.map { it.name }}
             """.trimIndent()
)
