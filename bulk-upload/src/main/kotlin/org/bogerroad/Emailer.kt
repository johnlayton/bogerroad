package org.bogerroad

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import java.nio.charset.Charset
import java.util.Properties
import java.util.concurrent.TimeUnit

data class EmailMessage(
    val firstName: String,
    val lastName: String,
    val email: String,
    val mobile: String,
    val message: String
)

class Emailer {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            val props = Properties()
            props["bootstrap.servers"] = "kafka:9092"
            props["acks"] = "all"
            props["retries"] = 0
            props["batch.size"] = 16384
            props["linger.ms"] = 1
            props["buffer.memory"] = 33554432
            props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
            props["value.serializer"] = "io.confluent.kafka.serializers.KafkaJsonSerializer"

            KafkaProducer<String, EmailMessage>(props).use { producer ->
                for (i in 0..99) {
                    val record = ProducerRecord(
                        "topic1", 0, "a", EmailMessage(
                            firstName = "firstName",
                            lastName = "lastName",
                            email = "firstName.lastName@company.com",
                            mobile = "+61410001000",
                            message = "Hello firstName lastName"
                        ), listOf(
                            RecordHeader(
                                "__TypeId__", "org.bogerroad.EmailMessage".toByteArray(Charset.defaultCharset())
                            )
                        )
                    )
                    println(producer.send(record)[5, TimeUnit.SECONDS])
                }
            }
        }
    }
}

/*
fun main(args : Array<String>) {
    Server()
}
*/
