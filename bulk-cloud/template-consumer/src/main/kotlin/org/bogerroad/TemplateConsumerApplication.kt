package org.bogerroad

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import net.logstash.logback.argument.StructuredArguments.v
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.util.function.Consumer
import java.io.Serializable as JavaSerializable

@SpringBootApplication
class TemplateConsumerApplication {

//    @Bean
//    fun emailTemplateConsumer(): Consumer<EmailTemplate> {
//        return Consumer {
//            logger.warn("Template id: ${it.id}, text: ${it.text}")
//        }
//    }

//
//    @Bean
//    fun emailMessageConsumer(): Consumer<EmailMessage> =
//        Consumer {
//            logger.warn("Message to: ${it.firstName} ${it.lastName}, message: ${it.message}", v("message", it))
//        }
//
//    companion object {
//        val logger: Logger by lazy {
//            LoggerFactory.getLogger(TemplateConsumerApplication::class.java)
//        }
//    }
}

//@Component
//class EmailTemplateConsumer : Consumer<EmailTemplate> {
//    override fun accept(it: EmailTemplate) {
////        TODO("Not yet implemented")
//        logger.warn("Template id: ${it.id}, text: ${it.text}")
//    }
//
//    companion object {
//        val logger: Logger by lazy {
//            LoggerFactory.getLogger(EmailTemplateConsumer::class.java)
//        }
//    }
//}


fun main(args: Array<String>) {
    runApplication<TemplateConsumerApplication>(*args)
}

data class EmailTemplate @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @param:JsonProperty("id") val id: String,
    @param:JsonProperty("text") val text: String
) : JavaSerializable

data class EmailMessage @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @param:JsonProperty("firstName") val firstName: String,
    @param:JsonProperty("lastName") val lastName: String,
    @param:JsonProperty("email") val email: String,
    @param:JsonProperty("mobile") val mobile: String,
    @param:JsonProperty("message") val message: String
) : JavaSerializable

@Configuration
class TemplateConfiguration() {

    @Bean
    fun emailTemplateConsumer(): Consumer<EmailTemplate> {
        return Consumer {
            logger.warn("Template id: ${it.id}, text: ${it.text}")
        }
    }

    companion object {
        val logger: Logger by lazy {
            LoggerFactory.getLogger(TemplateConfiguration::class.java)
        }
    }
}

@Configuration
class MessageConfiguration() {

    @Bean
    fun emailMessageConsumer(): Consumer<EmailMessage> {
        return Consumer {
            logger.warn("Message to: ${it.firstName} ${it.lastName}, message: ${it.message}", v("message", it))
        }
    }

    companion object {
        val logger: Logger by lazy {
            LoggerFactory.getLogger(MessageConfiguration::class.java)
        }
    }
}
