import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import com.google.protobuf.gradle.*

plugins {
    id("java")
    id("org.springframework.boot")
/*
    TODO: Add native image support
    id("org.springframework.experimental.aot") version "0.10.0"
*/
    id("io.spring.dependency-management")
    id("com.google.protobuf") version "0.8.16"

    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.20"
}

/*
java {
    targetCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
}
*/

repositories {
    maven("https://repo.spring.io/release")
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}

dependencies {
    implementation(project(":bulk-plan:common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.mariadb.jdbc:mariadb-java-client:1.5.7")
    implementation("org.postgresql:postgresql:42.2.18")
    implementation("io.github.lognet:grpc-spring-boot-starter:4.5.0")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    implementation("com.jayway.jsonpath:json-path")
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}

/*
TODO: Add native image support
tasks.getByName<BootBuildImage>("bootBuildImage") {
    builder = "paketobuildpacks/builder:tiny"
    environment = mapOf("BP_NATIVE_IMAGE" to "true")
}
*/

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.14.0"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.35.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}

sourceSets {
    val main by getting { }
    main.java.srcDirs("build/generated/source/proto/main/java")
    main.java.srcDirs("build/generated/source/proto/main/grpc")
}
