package org.bogerroad

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.TemplateFunction
import io.grpc.stub.StreamObserver
import net.logstash.logback.argument.StructuredArguments.kv
import net.logstash.logback.argument.StructuredArguments.v
import org.apache.kafka.clients.admin.NewTopic
import org.hibernate.annotations.GenericGenerator
import org.lognet.springboot.grpc.GRpcService
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Exchange
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.ClassMapper
import org.springframework.amqp.support.converter.DefaultClassMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.web.JsonPath
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.Serializable
import java.io.StringReader
import java.io.StringWriter
import java.util.Date
import java.util.Optional
import java.util.UUID
import javax.annotation.PostConstruct
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@SpringBootApplication
class StreamApplication

fun main(args: Array<String>) {
    runApplication<StreamApplication>(*args)
}

@Component
class TemplateLoader(private val repository: TemplateRepository) : CommandLineRunner {
    override fun run(vararg args: String?) {
        listOf(
                Template(text = "Hello {{ name }}!"),
                Template(text = "Bonjour {{ name }}!"),
                Template(text = "Privet {{ name }}!"),
        ).forEach {
            repository.save(it)
        }
    }
}

@RestController("template")
class TemplateResource(private val repository: TemplateRepository) {
    @GetMapping
    fun index(): List<Template> = repository.findAll()
}

@Service
class TemplateService(private val repository: TemplateRepository) {
    fun save(template: Template): Template = repository.save(template);
    fun findById(id: String): Optional<Template> = repository.findById(id);
}

@GRpcService
class MessageStreamService(
        private val service: TemplateService,
        private val amqpTemplate: AmqpTemplate,
        private val kafkaTemplate: KafkaTemplate<String, EmailMessage>)
    : MessageServiceGrpc.MessageServiceImplBase() {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val mustache = DefaultMustacheFactory()

    override fun create(request: TemplateRequest, responseObserver: StreamObserver<TemplateResponse>) {
        logger.info("Template Request {}", v("request", request))
        val template = service.save(Template(text = request.template))
        val response = TemplateResponse.newBuilder()
                .setId(template.id)
                .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun send(responseObserver: StreamObserver<MessageResponse>): StreamObserver<MessageRequest> {
        return object : StreamObserver<MessageRequest> {
            private var count: Long = 0

            @Transactional
            override fun onNext(messageRequest: MessageRequest) {
                logger.info("Next {}", v("messageRequest", messageRequest))
                logger.info("Validate {}", v("dataMap", messageRequest.dataMap))
                val template = service.findById(messageRequest.templateId).map { it.text }.orElse("")
                val context = mutableMapOf(
                        "date" to Date(),
                        "bold" to TemplateFunction { input ->
                            String.format("<b>%s</b>", input)
                        }
                )
                context.putAll(messageRequest.dataMap)
                val message: String =
                        mustache.compile(StringReader(template), messageRequest.templateId)
                                .execute(StringWriter(), context)
                                .toString()
                logger.info("Message {}", v("message", message))
                responseObserver.onNext(
                        MessageResponse.newBuilder()
                                .setMessage(message)
                                .build()
                )
                val email = EmailMessage(
                        messageRequest.contact.firstName,
                        messageRequest.contact.lastName,
                        messageRequest.contact.email,
                        messageRequest.contact.mobile,
                        message
                )
                amqpTemplate.convertAndSend("bulk-messaging-exchange", "bulk-message-email-queue", email)
                kafkaTemplate.send("topic1", email)
                count++
            }

            override fun onError(throwable: Throwable) {
                logger.error("Error", throwable)
            }

            override fun onCompleted() {
                logger.info("Complete {}", kv("count", count))
                responseObserver.onCompleted()
            }
        }
    }
}

data class EmailMessage @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
        @param:JsonProperty("firstName") val firstName: String,
        @param:JsonProperty("lastName") val lastName: String,
        @param:JsonProperty("email") val email: String,
        @param:JsonProperty("mobile") val mobile: String,
        @param:JsonProperty("message") val message: String
) : Serializable

@Entity
@Table(name = "template")
data class Template(
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(
                name = "UUID",
                strategy = "org.hibernate.id.UUIDGenerator"
        )
        val id: String = UUID.randomUUID().toString(),
        val text: String
)

@Entity
@Table(name = "settlement")
data class Settlement(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    val id: String = UUID.randomUUID().toString(),
)

interface TemplateRepository : JpaRepository<Template, String>, JpaSpecificationExecutor<Template>

@Configuration
class KafkaConfiguration(val kafkaAdmin: KafkaAdmin) {
    @Bean
    fun topic(): NewTopic {
        return TopicBuilder.name("topic1")
                .partitions(10)
                .replicas(1)
                .build()
    }
}

@Configuration
class RabbitConfiguration(val amqpAdmin: AmqpAdmin) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun classMapper(): DefaultClassMapper {
        return DefaultClassMapper().also {
            it.setIdClassMapping(mapOf("emailMessage" to EmailMessage::class.java))
        }
    }

    @Bean
    fun jsonMessageConverter(objectMapper: ObjectMapper, classMapper: ClassMapper): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter(objectMapper).also {
            it.classMapper = classMapper
            it.setUseProjectionForInterfaces(true)
        }
    }

    @PostConstruct
    fun init() {
        try {
            logger.info(
                    "Creating directExchange: exchange={}, routingKey={}",
                    v("exchange", "bulk-messaging-exchange"),
                    v("routingKey", "bulk-message-email-queue")
            )

            val ex =
                    ExchangeBuilder.directExchange("bulk-messaging-exchange")
                            .durable(true)
                            .build<Exchange>()
            amqpAdmin.declareExchange(ex)

            val q = QueueBuilder.durable("bulk-message-email-queue")
                    .build()
            amqpAdmin.declareQueue(q)

            val b: Binding = BindingBuilder.bind(q)
                    .to(ex)
                    .with("bulk-message-email-queue")
                    .noargs()
            amqpAdmin.declareBinding(b)

            logger.info("Binding successfully created.")
        } catch (e: Exception) {
            logger.error("Exception when initializing RabbitMQ: {}", v("message", e.message))
        }
    }
}

interface EmailMessageProjection {
    @get:JsonPath("$.email")
    val email: String

    @get:JsonPath("$.message")
    val message: String
}

@Component
class RabbitComponent {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = ["bulk-message-email-queue"])
    fun projection(projection: EmailMessageProjection) {

        logger.info("=========================================================")
        logger.info("Message:")
        logger.info("\tAddress: {}", v("email", projection.email))
        logger.info("\tMessage: {}", v("message", projection.message))
        logger.info("=========================================================")

    }
}

@Component
class KafkaComponent {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(id = "myId", topics = ["topic1"])
    fun listen(value: EmailMessage) {
        logger.info("=========================================================")
        logger.info("Message: {}", value)
        logger.info("=========================================================")
    }
}