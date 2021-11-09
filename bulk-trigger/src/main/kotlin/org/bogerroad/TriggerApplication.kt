package org.bogerroad

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import net.logstash.logback.argument.StructuredArguments.v
import org.hibernate.annotations.GenericGenerator
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.io.Serializable
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.sql.DataSource

@SpringBootApplication
class TriggerApplication

fun main(args: Array<String>) {
    runApplication<TriggerApplication>(*args)
}

@Configuration
class LocalTriggerConfiguration(
//    @Qualifier("primaryDataSource") private val primaryDataSource: DataSource,
//    @Qualifier("quartzDataSource") private val quartzDataSource: DataSource
) {
    init {
//        logger.info("Primary Datasource: ")
//        logger.info(primaryDataSource.connection.metaData.url)
//        logger.info("Quartz Datasource: ")
//        logger.info(quartzDataSource.connection.metaData.url)
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(LocalTriggerConfiguration::class.java) }
    }
}

//@Component
//class SettlementLoader(private val repository: SettlementRepository) : CommandLineRunner {
//    override fun run(vararg args: String?) {
//        repository.save(Settlement())
//
////        listOf(
////            Template(text = "Hello {{ name }}!"),
////            Template(text = "Bonjour {{ name }}!"),
////            Template(text = "Privet {{ name }}!"),
////        ).forEach {
////            repository.save(it)
////        }
//    }
//}


data class EmailMessage @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @param:JsonProperty("firstName") val firstName: String,
    @param:JsonProperty("lastName") val lastName: String,
    @param:JsonProperty("email") val email: String,
    @param:JsonProperty("mobile") val mobile: String,
    @param:JsonProperty("message") val message: String
) : Serializable

@Component
class TriggerJob(private val triggerService: TriggerService) : Job {
    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        triggerService.work(context.jobDetail.jobDataMap.getString("title"))
    }
}

@Service
class TriggerService(private val kafkaTemplate: KafkaTemplate<String, EmailMessage>) {
    fun work(details: String) {
        val email = EmailMessage(
            firstName = "John",
            lastName = "Layton",
            email = "johnstewartlayton@gmail.com",
            mobile = "0410001000",
            message = "Hello John"
        )

        logger.info("=========================================================")
        logger.info("Trigger {} -> Send Email: {}", details, v("email", email))
        logger.info("=========================================================")


//        settlementRepository.findByStatus()
        kafkaTemplate.send("topic1", email)
    }

    companion object {
        val logger: Logger by lazy {
            LoggerFactory.getLogger(TriggerService::class.java)
        }
    }
}

//@Component
//class TemplateLoader(private val triggerProperties: TriggerProperties,
//                     private val schedulerFactoryBean: SchedulerFactoryBean
//) : CommandLineRunner {
//    override fun run(vararg args: String?) {
//        logger.info("=========================================================")
//        logger.info("Trigger Properties: {}", triggerProperties)
//        logger.info("=========================================================")
//
//
////        val jobDetail = JobBuilder.newJob(TriggerJob::class.java)
////            .withIdentity(triggerProperties.name, triggerProperties.name)
////            .usingJobData("title", triggerProperties.name)
////            .build()
////
////        val trigger = SimpleTriggerFactoryBean().let { trigger ->
////            trigger.setName(triggerProperties.name)
////            trigger.setStartTime(Date())
////            trigger.setRepeatInterval(10 * 1000)
////            trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
////            trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)
////            trigger.afterPropertiesSet()
////            trigger.getObject()
////        }
////
////        schedulerFactoryBean.scheduler.scheduleJob(jobDetail, trigger)
//
//    }
//    companion object {
//        val logger: Logger by lazy {
//            LoggerFactory.getLogger(TemplateLoader::class.java)
//        }
//    }
//}

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

interface SettlementRepository : JpaRepository<Settlement, String>, JpaSpecificationExecutor<Settlement>


/*
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
*/

@Component
class KafkaComponent {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(id = "TriggerApplicationMessageListener", topics = ["topic1"])
    fun listen(value: EmailMessage) {
        logger.info("=========================================================")
        logger.info("Message: {}", value)
        logger.info("=========================================================")
    }
}
