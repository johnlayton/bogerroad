package org.bogerroad

import com.fasterxml.jackson.databind.ObjectMapper
import io.grpc.stub.StreamObserver
import net.logstash.logback.argument.StructuredArguments
import org.hibernate.annotations.GenericGenerator
import org.lognet.springboot.grpc.GRpcService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Exchange
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.ClassMapper
import org.springframework.amqp.support.converter.DefaultClassMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.web.JsonPath
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID
import javax.annotation.PostConstruct
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

@SpringBootApplication
class BulkPlanApiApplication {

}

fun main(args: Array<String>) {
    runApplication<BulkPlanApiApplication>(*args)
}

@Entity
@Table(name = "plans")
data class Plan(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    val id: String = UUID.randomUUID().toString(),
    val route: String? = null
) {
}

interface PlanRepository : JpaRepository<Plan, String>, JpaSpecificationExecutor<Plan>

@Service
class PlanService(private val repository: PlanRepository) {
    fun save(planning: Plan): Plan = repository.save(planning);
    fun findById(id: String): Optional<Plan> = repository.findById(id);
}

@GRpcService
class MessageStreamService(
    private val service: PlanService,
    private val amqpTemplate: AmqpTemplate
) : PlanningServiceGrpc.PlanningServiceImplBase() {

    override fun create(request: PlanRequest, responseObserver: StreamObserver<PlanResponse>) {
        logger.info("Planning Request {}", StructuredArguments.v("request", request))
        val planning = service.save(Plan())
        amqpTemplate.convertAndSend(
            "create", CreatePlan(
                reference = planning.id
            )
        )
        val response = PlanResponse.newBuilder()
            .setReference(planning.id)
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    companion object {
        val logger by LoggerDelegate()
    }
}

@Configuration
class RabbitConfiguration(val amqpAdmin: AmqpAdmin) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun classMapper(): DefaultClassMapper {
        return DefaultClassMapper().also { mapper ->
            mapper.setIdClassMapping(
                mapOf(
                    "createPlan" to CreatePlan::class.java,
                    "createdPlan" to CreatedPlan::class.java
                )
            )
        }
    }

    @Bean
    fun jsonMessageConverter(objectMapper: ObjectMapper, classMapper: ClassMapper): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter(objectMapper).also { converter ->
            converter.setClassMapper(classMapper)
            converter.setUseProjectionForInterfaces(true)
        }
    }

    @PostConstruct
    fun init() {
        try {
            logger.info(
                "Creating directExchange: exchange={}, routingKey={}",
                StructuredArguments.v("exchange", "planning"),
                StructuredArguments.v("routingKey", "create"),
                StructuredArguments.v("routingKey", "created")
            )

            val ex =
                ExchangeBuilder.directExchange("planning")
                    .durable(true)
                    .build<Exchange>()
            amqpAdmin.declareExchange(ex)

            val create = QueueBuilder.durable("create")
                .build()
            amqpAdmin.declareQueue(create)

            val created = QueueBuilder.durable("created")
                .build()
            amqpAdmin.declareQueue(created)

            amqpAdmin.declareBinding(
                BindingBuilder
                    .bind(create)
                    .to(ex)
                    .with("create")
                    .noargs()
            )

            amqpAdmin.declareBinding(
                BindingBuilder
                    .bind(created)
                    .to(ex)
                    .with("created")
                    .noargs()
            )

            logger.info("Binding successfully created.")
        } catch (e: Exception) {
            logger.error("Exception when initializing RabbitMQ: {}", StructuredArguments.v("message", e.message))
        }
    }
}

/*
interface CreatedPlanProjection {
    @get:JsonPath("$.reference")
    val reference: String

    @get:JsonPath("$.routing")
    val routing: String
}

@Component
class RabbitComponent(val service: PlanService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = ["created"])
    fun projection(projection: CreatedPlanProjection) {
        logger.info("=========================================================")
        logger.info("Plan Created:")
        logger.info("\tReference: {}", StructuredArguments.v("reference", projection.reference))
        logger.info("\tRouting: {}", StructuredArguments.v("routing", projection.routing))
        logger.info("=========================================================")
    }
}
*/

class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
    override fun getValue(thisRef: R, property: KProperty<*>) =
        LoggerFactory.getLogger(getClassForLogging(thisRef.javaClass))
}

fun <T : Any> getClassForLogging(javaClass: Class<T>): Class<*> {
    return javaClass.enclosingClass?.takeIf {
        it.kotlin.companionObject?.java == javaClass
    } ?: javaClass
}
