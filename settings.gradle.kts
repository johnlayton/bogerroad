pluginManagement {
    repositories {
        maven("https://repo.spring.io/release")
        maven("https://repo.spring.io/snapshot")
        maven("https://repo.spring.io/milestone")
        mavenCentral()
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
    "bulk-trait",
//    "bulk-plan:api",
//    "bulk-plan:common",
//    "bulk-plan:engine",
////    "bulk-trace:api",
////    "bulk-cloud:template-consumer",
//    "bulk-stream",
//    "bulk-graph:simple",
////    "bulk-graph:webmvc-http",
//    "bulk-graph:kickstart",
//    "bulk-auth",
//    "bulk-auth:auth-server",
//    "bulk-auth:res-server",
//    "bulk-auth:api-server",
//    "bulk-auth:login-server",
//    "bulk-auth:login-client",
//    "bulk-scratch",
//    "bulk-test",
//    "bulk-cdktf:kotlin-docker",
//    "bulk-cdktf:kotlin-aws"
)
