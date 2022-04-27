import org.jetbrains.kotlin.kapt3.base.Kapt.kapt
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
/*
    TODO: Add native image support
    id("org.springframework.experimental.aot") version "0.10.0"
*/
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa") version "1.5.21"
    kotlin("kapt")
}

/*
repositories {
    maven("https://repo.spring.io/release")
    maven("https://repo.spring.io/snapshot")
    mavenCentral()
}
*/

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    implementation("org.springframework.data:spring-data-commons")

    implementation("org.postgresql:postgresql:42.2.18")

    implementation("net.logstash.logback:logstash-logback-encoder:6.3")

    implementation(kotlin("reflect"))

    kapt("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
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
//    annotation("javax.persistence.Embeddable")
//    annotation("javax.persistence.MappedSuperclass")
}

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
