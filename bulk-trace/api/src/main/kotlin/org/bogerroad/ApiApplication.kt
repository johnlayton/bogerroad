package org.bogerroad

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import net.logstash.logback.argument.StructuredArguments.v
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.sleuth.annotation.NewSpan
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.io.Serializable

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}

@OpenAPIDefinition(
    info = Info(
        title = "**The Title Is Missing",
        description = "**The Description Is Missing",
        version = "**The Version Is Missing"
    ),
    servers = [
        Server(description = "localhost", url = "http://localhost:8080")
    ]
)
@Component
class ApiDefinition

@RestController
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
}

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

