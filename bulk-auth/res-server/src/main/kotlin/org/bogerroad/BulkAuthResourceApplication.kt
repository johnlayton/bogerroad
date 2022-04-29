package org.bogerroad

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import net.logstash.logback.argument.StructuredArguments.v
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.io.Serializable

@SpringBootApplication
class BulkAuthResourceApplication

fun main(args: Array<String>) {
    runApplication<BulkAuthResourceApplication>(*args)
}

@EnableWebSecurity
class ResourceServerConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        return http.also {
            it.mvcMatcher("/api/**")
              .authorizeRequests()
              .mvcMatchers("/api/**")
              .access("hasAuthority('SCOPE_api.read')")
              .and()
              .oauth2ResourceServer()
              .jwt()
            it.mvcMatcher("/articles/**")
                .authorizeRequests()
                .mvcMatchers("/articles/**")
                .access("hasAuthority('SCOPE_articles.read')")
                .and()
                .oauth2ResourceServer()
                .jwt()
        }.build()
    }
}

@RestController
@RequestMapping("articles")
class ArticlesResource {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun get(authentication: Authentication) : Array<String> {
        logger.info("=========================================================")
        logger.info("Call to API /articles")
        logger.info("=========================================================")
        return arrayOf("Article 1", "Article 2", "Article 3")
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(ArticlesResource::class.java) }
    }
}

@RestController
@RequestMapping("api")
class ApiResource(private val apiService: ApiService) {

    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun get(@PathVariable("id") id : String) : ApiMessage {
        logger.info("=========================================================")
        logger.info("Call to API /api/{}", v("id", id))
        logger.info("=========================================================")
        return ApiMessage("Got by Id : $id")
    }

    @GetMapping
    fun get(): ApiMessage {
        logger.info("=========================================================")
        logger.info("Call to API /api")
        logger.info("=========================================================")
        return apiService.message()
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(ApiResource::class.java) }
    }
}

@Service
class ApiService {
    fun message(): ApiMessage = ApiMessage(message = "hello from api")
}

data class ApiMessage @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @param:JsonProperty("message") val message: String
) : Serializable
