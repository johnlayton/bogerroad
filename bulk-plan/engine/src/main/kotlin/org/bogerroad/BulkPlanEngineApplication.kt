package org.bogerroad

import com.fasterxml.jackson.databind.ObjectMapper
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpAdmin
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
import org.springframework.data.web.JsonPath
import org.springframework.stereotype.Component
import java.util.UUID
import javax.annotation.PostConstruct
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

@SpringBootApplication
class BulkPlanApiApplication {

}

fun main(args: Array<String>) {
    runApplication<BulkPlanApiApplication>(*args)
}

@Configuration
class RabbitConfiguration(val amqpAdmin: AmqpAdmin) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val routing : String = UUID.randomUUID().toString()

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
                StructuredArguments.v("createRoutingKey", "create"),
                StructuredArguments.v("createdRoutingKey", "created"),
                StructuredArguments.v("routingKey", routing)
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

            val control = QueueBuilder.durable(routing)
                .build()
            amqpAdmin.declareQueue(control)

            amqpAdmin.declareBinding(
                BindingBuilder
                    .bind(control)
                    .to(ex)
                    .with(routing)
                    .noargs()
            )

            logger.info("Binding successfully created.")
        } catch (e: Exception) {
            logger.error("Exception when initializing RabbitMQ: {}", StructuredArguments.v("message", e.message))
        }
    }
}

interface CreatePlanProjection {
    @get:JsonPath("$.reference")
    val reference: String
}

@Component
class RabbitComponent() {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = ["create"])
    fun projection(projection: CreatePlanProjection) {
        logger.info("=========================================================")
        logger.info("Plan Create:")
        logger.info("\tReference: {}", StructuredArguments.v("reference", projection.reference))
        logger.info("=========================================================")
    }
}

class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
    override fun getValue(thisRef: R, property: KProperty<*>) =
        LoggerFactory.getLogger(getClassForLogging(thisRef.javaClass))
}

fun <T : Any> getClassForLogging(javaClass: Class<T>): Class<*> {
    return javaClass.enclosingClass?.takeIf {
        it.kotlin.companionObject?.java == javaClass
    } ?: javaClass
}
