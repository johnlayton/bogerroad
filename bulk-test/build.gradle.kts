import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("java")
    id("org.springframework.boot")
/*
    TODO: Add native image support
    id("org.springframework.experimental.aot") version "0.10.0"
*/
    id("io.spring.dependency-management")

    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    kotlin("kapt") //version "1.6.10"
}

/*
java {
    targetCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
}
*/

/*
repositories {
    maven("https://repo.spring.io/release")
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}
*/
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.7.0-SNAPSHOT")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.4")
//        mavenBom("org.springframework.cloud:spring-cloud-sleuth-otel-dependencies:1.0.0-SNAPSHOT")
    }
}

repositories {
    maven("https://repo.spring.io/release")
    maven("https://repo.spring.io/snapshot")
    maven("https://repo.spring.io/milestone")
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.boot:spring-boot-starter-amqp")
//    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.mariadb.jdbc:mariadb-java-client:1.5.7")
    implementation("org.postgresql:postgresql:42.3.3")
    implementation("com.h2database:h2:2.1.212")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    implementation("com.jayway.jsonpath:json-path")
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    kapt("org.hibernate:hibernate-jpamodelgen:5.4.30.Final")
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

sourceSets {
    val main by getting { }
    main.java.srcDirs("build/generated/source/proto/main/java")
    main.java.srcDirs("build/generated/source/proto/main/grpc")
}
