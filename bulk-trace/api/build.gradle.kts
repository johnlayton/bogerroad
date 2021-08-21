plugins {
    id("java")
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.2"
    id("com.gorylenko.gradle-git-properties") version "2.3.1"

//	id("com.palantir.git-version") version "0.12.3"
    id("fr.brouillard.oss.gradle.jgitver") version "0.10.0-rc03"

    kotlin("jvm") version "1.5.10"
    kotlin("plugin.spring") version "1.5.10"
}

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}

repositories {
    maven("https://repo.spring.io/release")
    maven("https://repo.spring.io/snapshot")
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

/*
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.cloud:spring-cloud-sleuth-zipkin")
*/

/*
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth") {
        exclude(module = "spring-cloud-sleuth-brave")
    }
    implementation("org.springframework.cloud:spring-cloud-sleuth-otel-autoconfigure")
    implementation("org.springframework.cloud:spring-cloud-sleuth-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin")
    implementation("io.grpc:grpc-netty-shaded:1.37.0")
*/

    implementation("org.springdoc:springdoc-openapi-ui:1.5.0")
/*
    implementation("org.springframework.kafka:spring-kafka")
*/
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation(kotlin("reflect"))
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.5.3")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.3")
        mavenBom("org.springframework.cloud:spring-cloud-sleuth-otel-dependencies:1.0.0-SNAPSHOT")
    }
}

openApi {
    outputFileName.set("openapi.yaml")
    apiDocsUrl.set("http://localhost:8080/v3/api-docs.yaml")
//    forkProperties = "-Dspring.profiles.active=zipking"
}

springBoot {
    buildInfo()
}

jgitver {
    strategy = fr.brouillard.oss.jgitver.Strategies.MAVEN
}
