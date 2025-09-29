plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
//    alias(libs.plugins.kotlin.jvm)
    kotlin("jvm") version "2.2.20"
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id( "com.gradleup.shadow") version "9.2.2"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("com.dorongold.task-tree") version "4.0.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation("javax.vecmath:vecmath:1.5.2")
    implementation("me.saharnooby:qoi-java:1.2.1")
    implementation("org.sejda.imageio:webp-imageio:0.1.6")
    implementation("com.google.guava:guava:33.5.0-jre")
    implementation("commons-io:commons-io:2.20.0")

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

application {
    mainClass = "com.marginallyclever.showthr.ShowTHR"
//    group = "com.marginallyclever"
//    version = "0.0.1-SNAPSHOT"
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
    //    args( "/Users/douglas_bullard/dev/github/douglasBullard/sisyphus-table-pattern-maker/images4")
}

tasks.register<JavaExec>("runAnalyzeOutput") {
    group = "application"
    description = "Runs AnalyzeOutput"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.nurflugel.showthr.AnalyzeOutput"
}
