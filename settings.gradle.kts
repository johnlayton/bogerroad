pluginManagement {
    repositories {
        maven("https://repo.spring.io/release")
        maven("https://repo.spring.io/snapshot")
        maven("https://plugins.gradle.org/m2")
    }
}

rootProject.name = "bulk"

include(
    "bulk-plan:api",
    "bulk-trace:api",
    "bulk-stream",
    "bulk-cloud:template-consumer"
)
