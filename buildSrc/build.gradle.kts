plugins {
    `kotlin-dsl`
}

configure<KotlinDslPluginOptions> {
    experimentalWarning.set(false)
}

repositories {
    jcenter()
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
