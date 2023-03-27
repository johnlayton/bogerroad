plugins {
    id("java")
//    id("org.springframework.boot") version "2.7.0-SNAPSHOT" apply false
//    id("org.springframework.boot") version "2.5.4" apply false
    id("org.springframework.boot") version "2.7.0" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false

    id("com.github.johnrengelman.processes") version "0.5.0" apply false
    id("org.springdoc.openapi-gradle-plugin") version "1.3.2" apply false
    id("com.gorylenko.gradle-git-properties") version "2.4.1" apply false

    id("com.github.ben-manes.versions") version "0.41.0"

//	id("com.palantir.git-version") version "0.12.3"
    id("fr.brouillard.oss.gradle.jgitver") version "0.10.0-rc03"

    kotlin("jvm") version "1.8.10" apply false
    kotlin("kapt") version "1.8.10" apply false
    kotlin("plugin.spring") version "1.8.10" apply false

    id("github") apply false
}

jgitver {
    strategy = fr.brouillard.oss.jgitver.Strategies.MAVEN
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    maven("https://repo.spring.io/release")
    maven("https://repo.spring.io/snapshot")
    mavenCentral()
}

//allprojects {
//  apply(plugin = "com.github.ben-manes.versions")
//}

//dependencies {
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-actuator")
//
///*
//    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
//    implementation("org.springframework.cloud:spring-cloud-sleuth-zipkin")
//*/
//
///*
//    implementation("org.springframework.cloud:spring-cloud-starter-sleuth") {
//        exclude(module = "spring-cloud-sleuth-brave")
//    }
//    implementation("org.springframework.cloud:spring-cloud-sleuth-otel-autoconfigure")
//    implementation("org.springframework.cloud:spring-cloud-sleuth-otel")
//    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
//    implementation("io.opentelemetry:opentelemetry-exporter-zipkin")
//    implementation("io.grpc:grpc-netty-shaded:1.37.0")
//*/
//
//    implementation("org.springdoc:springdoc-openapi-ui:1.5.0")
///*
//    implementation("org.springframework.kafka:spring-kafka")
//*/
//    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
//    implementation(kotlin("reflect"))
//}

//dependencyManagement {
//    imports {
//        mavenBom("org.springframework.boot:spring-boot-dependencies:2.5.3")
//        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.3")
//        mavenBom("org.springframework.cloud:spring-cloud-sleuth-otel-dependencies:1.0.0-SNAPSHOT")
//    }
//}

//openApi {
//    outputFileName.set("openapi.yaml")
//    apiDocsUrl.set("http://localhost:8080/v3/api-docs.yaml")
////    forkProperties = "-Dspring.profiles.active=zipkin"
//}
//
//springBoot {
//    buildInfo()
//}
//
//
//tasks.register<Copy>("gh-pages") {
//    group = "documentation"
//    dependsOn("generateOpenApiDocs")
//    from(layout.projectDirectory.dir("docs"))
//    from(layout.buildDirectory.file("openapi.yaml"))
//    into(layout.buildDirectory.dir("docs"))
//}
