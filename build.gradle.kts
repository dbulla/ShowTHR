plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("com.gradleup.shadow") version "9.0.2"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("com.dorongold.task-tree") version "4.0.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("javax.vecmath:vecmath:1.5.2")
    implementation("me.saharnooby:qoi-java:1.2.1")
    implementation("org.sejda.imageio:webp-imageio:0.1.6")
    implementation("com.google.guava:guava:33.4.8-jre")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.marginallyclever.showthr.ShowTHR"
//    mainClass = "com.nurflugel.showthr.Renamer"
//    group = "com.marginallyclever"
//    version = "0.0.1-SNAPSHOT"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
