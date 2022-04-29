package org.bogerroad

import net.logstash.logback.argument.StructuredArguments.v
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class BulkAuthApiApplication

fun main(args: Array<String>) {
    runApplication<BulkAuthApiApplication>(*args)
}

@EnableWebSecurity
class BulkAuthApiApplicationSecurityConfiguration {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        return http.also {
//            it.mvcMatcher("/api/**")
//                .authorizeHttpRequests { it.anyRequest().authenticated() }
//                .oauth2Login { it.loginPage("/oauth2/authorization/api-client-oidc") }
//                .oauth2Client(withDefaults())
//            it.mvcMatcher("/articles/**")
//                .authorizeHttpRequests { it.anyRequest().authenticated() }
//                .oauth2Login { it.loginPage("/oauth2/authorization/articles-client-oidc") }
//                .oauth2Client(withDefaults())
//        }.build()

//        http {
//            oauth2Login {
//
//            }
//        }
//
        http.authorizeHttpRequests { it.mvcMatchers("/api/**").authenticated() }
            .oauth2Login { it.loginPage("/oauth2/authorization/api-client-oidc") }
            .oauth2Client(withDefaults());
        http.authorizeHttpRequests { it.mvcMatchers("/articles/**").authenticated() }
            .oauth2Login {
                it.loginPage("/oauth2/authorization/articles-client-oidc")
            }
            .oauth2Client(withDefaults())
        return http.build()
    }
}

@Configuration
class BulkAuthApiApplicationWebClientConfiguration {

    @Bean
    fun webClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
        val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            .build()
    }

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientRepository: OAuth2AuthorizedClientRepository
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .refreshToken()
            .build()
        val authorizedClientManager = DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository
        )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}

@RestController
@RequestMapping("api")
class ApiResource(private val webClient: WebClient) {

    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun get(
        @PathVariable("id") id: String,
        @RegisteredOAuth2AuthorizedClient("api-client-authorization-code") authorizedClient: OAuth2AuthorizedClient
    ): String {
        logger.info("=========================================================")
        logger.info("Call to API /api/{}", v("id", id))
        logger.info("Token {}", v("token", authorizedClient.accessToken.tokenValue))
        logger.info("=========================================================")
        return webClient
            .get()
            .uri("http://127.0.0.1:8090/api/1")
            .attributes(oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()!!
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(ApiResource::class.java) }
    }
}

@RestController
class ArticlesResource(private val webClient: WebClient) {

    @GetMapping(value = ["/articles"])
    fun getArticles(
        @RegisteredOAuth2AuthorizedClient("articles-client-authorization-code") authorizedClient: OAuth2AuthorizedClient

    ): Array<String> {
        logger.info("=========================================================")
        logger.info("Call to API /articles")
        logger.info("=========================================================")
        return webClient
            .get()
            .uri("http://127.0.0.1:8090/articles")
            .attributes(oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(Array<String>::class.java)
            .block()!!
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(ArticlesResource::class.java) }
    }
}
