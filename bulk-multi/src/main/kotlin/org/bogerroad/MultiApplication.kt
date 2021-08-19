package org.bogerroad

import org.hibernate.annotations.GenericGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table


@SpringBootApplication
class MultiApplication

fun main(args: Array<String>) {
    runApplication<MultiApplication>(*args)
}

@Entity
@Table(name = "settlement")
data class Settlement(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    val id: String = UUID.randomUUID().toString()
)

interface SettlementRepository : JpaRepository<Settlement, String>, JpaSpecificationExecutor<Settlement>

@Component
class SettlementLoader(private val repository: SettlementRepository) : CommandLineRunner {
    override fun run(vararg args: String?) {
        repository.save(Settlement())
    }
}

