package org.bogerroad

import org.hibernate.annotations.GenericGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.transaction.Transactional
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject


@SpringBootApplication
class BulkTestApplication {
}

fun main(args: Array<String>) {
    runApplication<BulkTestApplication>(*args)
}

@Component
class Demo(
    private val firstRepository: FirstRepository,
    private val otherRepository: OtherRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {

        val one = otherRepository.save(Other(name = "1"))
        val two = otherRepository.save(Other(name = "2"))
        val three = otherRepository.save(Other(name = "3"))
        val four = otherRepository.save(Other(name = "4"));
        val five = otherRepository.save(Other(name = "5"))
        val six = otherRepository.save(Other(name = "6"))
        val seven = otherRepository.save(Other(name = "7"))
        val eight = otherRepository.save(Other(name = "8"))
        val nine = otherRepository.save(Other(name = "9"))

        firstRepository.saveAll(
            listOf(
                First(name = "1", others = setOf(one, two, three, four)),
                First(name = "2", others = setOf(three, four)),
                First(name = "3", others = setOf(two, three, four, five)),
                First(name = "4", others = setOf(three, four, five, six)),
                First(name = "5", others = setOf(five, six, seven, eight)),
                First(name = "6", others = setOf(seven, eight, nine)),
            )
        )

        println(firstRepository.findAll(containsOtherByName("2")))

        println(firstRepository.findAll(containsAllOtherByNames(setOf("4", "5"))))
        println(firstRepository.findAll(containsAllOtherByNames(setOf("3", "4"))))
        println(firstRepository.findAll(containsAnyOtherByNames(setOf("4", "5"))))
        println(firstRepository.findAll(containsAnyOtherByNames(setOf("4"))))
    }

    fun containsOtherByName(name : String) : Specification<First> {
        return Specification<First> { root, query, builder ->
            query.distinct(true);
            builder.equal(root.join(First_.others).get(Other_.name), name)
        }
    }

    fun containsAllOtherByNames(names : Set<String>) : Specification<First> {
        return names.map { name -> containsOtherByName(name) }.reduceRight { a, b -> a.and(b) }
    }

    fun containsAnyOtherByNames(names : Set<String>) : Specification<First> {
        return names.map { name -> containsOtherByName(name) }.reduceRight { a, b -> a.or(b) }
    }

    fun containsExactlyOtherByNames(names : Set<String>) : Specification<First> {
        return names.map { name -> containsOtherByName(name) }.reduceRight { a, b -> a.and(b) }
    }
}


@Entity
@Table(name = "first")
class First(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    val id: UUID = UUID.randomUUID(),
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val others: Set<Other>,
    val name: String? = null
) {
    override fun toString(): String {
        return "First(id=$id, others=$others, name=$name)"
    }
}

@Entity
@Table(name = "other")
class Other(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    val id: UUID = UUID.randomUUID(),
    val name: String? = null
) {
    override fun toString(): String {
        return "Other(id=$id, name=$name)"
    }
}

interface OtherRepository : JpaRepository<Other, UUID>, JpaSpecificationExecutor<Other>
interface FirstRepository : JpaRepository<First, UUID>, JpaSpecificationExecutor<First>

class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
    override fun getValue(thisRef: R, property: KProperty<*>) =
        LoggerFactory.getLogger(getClassForLogging(thisRef.javaClass))
}

fun <T : Any> getClassForLogging(javaClass: Class<T>): Class<*> {
    return javaClass.enclosingClass?.takeIf {
        it.kotlin.companionObject?.java == javaClass
    } ?: javaClass
}
