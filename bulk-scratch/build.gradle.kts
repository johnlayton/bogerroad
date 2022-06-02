//import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
//import com.google.protobuf.gradle.*

plugins {
    id("java")
    kotlin("jvm")
}

repositories {
    maven("https://repo.spring.io/release")
//    maven("https://packages.confluent.io/maven")
//    maven("https://maven.repository.redhat.com/earlyaccess/all")
//    maven("https://repository.mulesoft.org/nexus/content/repositories/public")
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("com.breinify:brein-time-utilities:1.7.0")
    implementation("org.mapstruct:mapstruct:1.4.2.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.4.2.Final")
//    implementation("org.apache.tika:tika-bundle:1.26")
//    implementation("org.apache.kafka:kafka-clients:2.8.0")
//    implementation("io.confluent:kafka-json-schema-serializer:6.2.0")
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.boot:spring-boot-starter-amqp")
//    implementation("org.springframework.data:spring-data-commons")
//    implementation("io.github.lognet:grpc-spring-boot-starter:4.5.0")
//    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
//    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
//    implementation("com.jayway.jsonpath:json-path")
//    implementation(kotlin("reflect"))
//    implementation("com.h2database:h2")
}

/*
TODO: Add native image support
tasks.getByName<BootBuildImage>("bootBuildImage") {
    builder = "paketobuildpacks/builder:tiny"
    environment = mapOf("BP_NATIVE_IMAGE" to "true")
}
*/

//allOpen {
//    annotation("javax.persistence.Entity")
//    annotation("javax.persistence.Embeddable")
//    annotation("javax.persistence.MappedSuperclass")
//}
//
//protobuf {
//    protoc {
//        artifact = "com.google.protobuf:protoc:3.14.0"
//    }
//    plugins {
//        id("grpc") {
//            artifact = "io.grpc:protoc-gen-grpc-java:1.35.0"
//        }
//    }
//    generateProtoTasks {
//        ofSourceSet("main").forEach {
//            it.plugins {
//                id("grpc")
//            }
//        }
//    }
//}
//
//sourceSets {
//    val main by getting { }
//    main.java.srcDirs("build/generated/source/proto/main/java")
//    main.java.srcDirs("build/generated/source/proto/main/grpc")
//}
