pluginManagement {
    repositories {
        maven("https://repo.spring.io/release")
        maven("https://repo.spring.io/snapshot")
        maven("https://repo.spring.io/milestone")
        maven("https://plugins.gradle.org/m2")
        gradlePluginPortal()
    }
}

//pluginManagement {
//    repositories {
//        maven { url 'https://repo.spring.io/release' }
//        maven { url 'https://repo.spring.io/milestone' }
//        maven { url 'https://repo.spring.io/snapshot' }
//        gradlePluginPortal()
//    }
//}


rootProject.name = "bulk"

include(
//    "bulk-multi",
//    "bulk-plan:api",
//    "bulk-plan:common",
//    "bulk-plan:engine",
//    "bulk-trace:api",
//    "bulk-stream",
//    "bulk-cloud:template-consumer",
    "bulk-graph:simple",
    "bulk-auth:auth-server",
    "bulk-auth:res-server",
    "bulk-auth:api-server"
)
