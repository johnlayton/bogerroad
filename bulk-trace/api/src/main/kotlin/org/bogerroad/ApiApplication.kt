package org.bogerroad

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.info.GitProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}

@OpenAPIDefinition(
    info = Info(
        title = "Workspace Service",
        description = "This is the Workspace service where one can create, update, get detail, list and search for workspaces.",
        version = " 1.0.0"
    ),
    servers = [
        Server(description = "localhost - 1", url = "http://localhost:8080"),
        Server(description = "localhost - 2", url = "http://localhost:8081")
    ]
)
@Component
class ApiDefinition

@RestController
@RequestMapping("/api/v1/workspaces")
interface WorkspaceResource {

    @PostMapping
    @Operation(
        description = "blah blah"
    )
    fun createWorkspace(
        @RequestHeader("X-Subscriber-ID") subscriberId: String,
        @Valid @RequestBody workspace: Workspace
    ): WorkspaceResponse = WorkspaceResponse(UUID.randomUUID())

}

@Service
class WorkspaceResourceImpl : WorkspaceResource {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun createWorkspace(subscriberId: String, workspace: Workspace): WorkspaceResponse {
        logger.info("Workspace created {}", kv("workspace", workspace, workspace.mortgageApplicationReference))
        return WorkspaceResponse(UUID.randomUUID())
    }
}

data class Workspace(
    val transactionType: TransactionType,
    val jurisdiction: Jurisdiction,
    val securityAddress: SecurityAddress,
    @field:NotBlank @field:Size(min = 0, max = 30) val mortgageApplicationReference: String
)

enum class TransactionType(val code: String) {
    REMORTGAGE("REMORTGAGE"),
    NEW_PURCHASE("NEW_PURCHASE")
}

enum class Jurisdiction(val code: String) {
    ENGLAND_WALES("ENGLAND_WALES")
}

data class WorkspaceResponse(
    val workspaceId: UUID
)

data class SecurityAddress(
    val postcode: String,
    val name: String? = null,
    val number: String? = null,
    val flat: String? = null,
    val street: String? = null,
    val town: String? = null
)

@Configuration
@EnableConfigurationProperties(ApiProperties::class)
class ApiConfiguration(
    private val buildProperties: BuildProperties,
    private val gitProperties: GitProperties
) {

    @Bean
    fun openApiCustomiser(apiProperties: ApiProperties): OpenApiCustomiser {
        return OpenApiCustomiser {
            it.info.title = apiProperties.title
            it.info.version = buildProperties.version
            it.tags = listOf(
                Tag().also {
                    it.name = "commit"
                    it.description = gitProperties.commitId
                }) + it.tags.orEmpty()
        }
    }

}

@ConstructorBinding
@ConfigurationProperties(prefix = "bulk.openapi")
data class ApiProperties(
    val title: String
)

/*@RestController
@RequestMapping("api")
interface ApiResource {
    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        extensions = [Extension(
            name = "x-amazon-apigateway-integration",
            properties = [
                ExtensionProperty(name = "uri", value = "\${host}/api/{id}"),
                ExtensionProperty(name = "passthroughBehavior", value = "when_no_match"),
                ExtensionProperty(name = "httpMethod", value = "GET"), ExtensionProperty(name = "type", value = "http_proxy"),
                ExtensionProperty(name = "requestParameters", value = "{ \"integration.request.path.id\" : \"method.request.path.id\"}", parseValue = true)
            ]
        )]
    )
    fun get(@PathVariable("id") id : String) : ApiMessage

    @GetMapping
    @Operation
    fun index(@RequestHeader("X-Amzn-Trace-Id") traceId: String): ApiMessage
}

@Component
class ApiResourceImpl(private val apiService: ApiService) : ApiResource {

    @NewSpan("get-by-id")
    override fun get(@PathVariable("id") id : String) : ApiMessage = ApiMessage("Got by Id : $id")

    override fun index(@RequestHeader("X-Amzn-Trace-Id") traceId: String): ApiMessage {
        logger.info("=========================================================")
        logger.info("Call to API {}", v("traceId", traceId))
        logger.info("=========================================================")
        return apiService.message()
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(ApiResource::class.java) }
    }
}*/

/*
@Service
class ApiService(private val kafkaTemplate: KafkaTemplate<String, ApiMessage>) {
    fun message(): ApiMessage {
        return ApiMessage(message = "hello from api").also {
            kafkaTemplate.send("bulk-trace-topic-1", it)
        }
    }
}

@Component
class ApiListener {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(id = "api-echo", topics = ["bulk-trace-topic-1"])
    fun listen(value: ApiMessage) {
        logger.info("=========================================================")
        logger.info("Message: {}", value)
        logger.info("=========================================================")
    }
}

data class ApiMessage @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @param:JsonProperty("message") val message: String
) : Serializable
*/

