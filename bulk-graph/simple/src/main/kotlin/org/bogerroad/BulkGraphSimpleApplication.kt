package org.bogerroad

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BulkGraphSimpleApplication

fun main(args: Array<String>) {
    runApplication<BulkGraphSimpleApplication>(*args)
}
