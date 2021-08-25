package org.bogerroad

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.expand
import org.gradle.kotlin.dsl.filter
import org.gradle.kotlin.dsl.register
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

// https://docs.gradle.org/current/userguide/custom_plugins.html

open class GithubExtension(
//    var greeting: String = "Hello",
//    var name: String = "buddy"
)

class GithubPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val github = project.extensions.create<GithubExtension>("GithubExtension")
        project.extensions.add("github", github)
        val task1 = project.tasks.register<Copy>("github-pages-openapi-spec") {
            group = "documentation"
            description = "Copy github documentation from the generated openapi.yaml"
            dependsOn(project.tasks.findByName("generateOpenApiDocs"))
            from(layout.buildDirectory.file("openapi.yaml"))
            into(layout.buildDirectory.dir("docs"))
        }
        val task2 = project.tasks.register<Copy>("github-pages-placeholder") {
            group = "documentation"
            description = "Copy github documentation from docs folder"
            from(layout.projectDirectory.dir("docs"))
            into(layout.buildDirectory.dir("docs"))
            expand(
                "copyright" to LocalDate.now().year.toString(),
//                "version" to project.version
            )
            expand(project.properties)
            filter(ReplaceTokens::class, "tokens" to mapOf(
                "generated" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "copyright" to LocalDate.now().year.toString()
//                "version" to project.version
            ))
        }
        project.tasks.register<Copy>("github-pages") {
            group = "documentation"
            description = "Copy github documentation from docs folder, include the generated openapi.yaml"
            dependsOn(task1, task2)
        }

    }
}

