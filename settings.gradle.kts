pluginManagement {
    repositories {
        maven("https://repo.spring.io/release")
        maven("https://repo.spring.io/snapshot")
        maven("https://plugins.gradle.org/m2")
    }
}

rootProject.name = "bulk"

include(
    "bulk-multi",
    "bulk-plan:api",
    "bulk-plan:common",
    "bulk-plan:engine",
    "bulk-trace:api",
    "bulk-stream",
    "bulk-cloud:template-consumer"
)
