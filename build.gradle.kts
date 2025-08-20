plugins {
    java
    id("io.quarkus") version "3.15.5"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))

    // REST Reactive (server) + JSON
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")

    // REST Client Reactive (+ Jackson)
    implementation("io.quarkus:quarkus-rest-client-reactive")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson")

    // Resilience y CLI
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
    // implementation("io.quarkus:quarkus-picocli") // Removed - using REST API instead

    // Cache
    implementation("io.quarkus:quarkus-caffeine")

    // CSV parser
    implementation("com.univocity:univocity-parsers:2.9.1")

    // Health check
    implementation("io.quarkus:quarkus-smallrye-health")
    
    // Metrics
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")

    // Test
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.wiremock:wiremock-standalone:3.9.1")
}

group = "org.ravenpack"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
