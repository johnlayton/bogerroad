plugins {
    `kotlin-dsl`
}

//configure<KotlinDslPluginOptions> {
//    experimentalWarning.set(false)
//}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        register("github") {
            id = "github"
            implementationClass = "org.bogerroad.GithubPlugin"
        }
    }
}
