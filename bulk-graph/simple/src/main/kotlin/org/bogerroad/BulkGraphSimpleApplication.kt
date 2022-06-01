package org.bogerroad

import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.execution.ExecutionId
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLScalarType
import org.hibernate.annotations.GenericGenerator
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.NestedRuntimeException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.graphql.data.GraphQlRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.execution.DataFetcherExceptionResolver
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.transaction.Transactional
import javax.transaction.Transactional.TxType.NOT_SUPPORTED
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import javax.validation.Validator
import kotlin.reflect.KClass


@SpringBootApplication
class BulkGraphSimpleApplication

fun main(args: Array<String>) {
    runApplication<BulkGraphSimpleApplication>(*args)
}

@Controller
class MainController(
    val workingSetRepository: WorkingSetRepository,
    val otherRepository: OtherRepository
) {
    @MutationMapping
    fun hello(@Argument name: String): String {
        throw RuntimeException("bob")
//        return "Hello, $name!"
    }

    @MutationMapping
    fun createOther(@Argument other: OtherIn): Other =
        otherRepository.save(Other(name = other.name, colour = other.colour));

    @MutationMapping
    fun createWorkingSet(@Argument name: String): WorkingSet =
        workingSetRepository.save(WorkingSet(name = name));
}

@Configuration
class ValidationConfig {
    @Bean
    fun defaultValidator(): Validator = LocalValidatorFactoryBean()
}

@Configuration
class GraphQlConfig {

    @Bean
    fun dataFetcherExceptionResolver(): DataFetcherExceptionResolver = CustomDataFetcherExceptionResolver

    object CustomDataFetcherExceptionResolver : DataFetcherExceptionResolverAdapter() {
        override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError = when (ex) {
            is NestedRuntimeException -> GraphqlErrorBuilder
                                                    .newError(env)
                                                    .errorType(ErrorType.ValidationError)
                                                    .message(ex.rootCause?.message ?: "")
                                                    .build()
            else -> GraphqlErrorBuilder
                .newError(env)
                .errorType(ErrorType.ValidationError)
                .message(ex.message)
                .build()
        }
    }

    @Bean
    fun sourceBuilderCustomizer(): GraphQlSourceBuilderCustomizer = GraphQlSourceBuilderCustomizer { builder ->
        builder.configureGraphQl { graphQlBuilder ->
            graphQlBuilder.executionIdProvider { _, _, _ ->
                ExecutionId.from(System.nanoTime().toString())
            }
/*
            graphQlBuilder.executionIdProvider({ query, operationName, context ->
                ExecutionId.from(System.nanoTime().toString())
            })
*/
        }
    }

    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer = RuntimeWiringConfigurer { builder ->
        builder.scalar(scalarUUID());
    }

    fun scalarUUID(): GraphQLScalarType {
        return GraphQLScalarType.newScalar()
            .name("UUID")
            .description("UUID Value")
            .coercing(UUIDCoercing)
            .build()
    }

    object UUIDCoercing : Coercing<UUID, String> {

        override fun parseValue(input: Any): UUID = UUID.fromString(serialize(input))

        override fun parseLiteral(input: Any): UUID {
            val uuidString = (input as? StringValue)?.value
            return UUID.fromString(uuidString)
        }

        override fun serialize(output: Any): String = output.toString()
    }
}

@Component
class DataLoader(
    private val workingSetRepository: WorkingSetRepository,
    private val otherRepository: OtherRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        workingSetRepository.save(WorkingSet(UUID.randomUUID(), "1"))
        workingSetRepository.save(WorkingSet(UUID.randomUUID(), "2"))
        workingSetRepository.save(WorkingSet(UUID.randomUUID(), "3"))
        otherRepository.save(Other(UUID.randomUUID(), "1", "#AA2222"))
        otherRepository.save(Other(UUID.randomUUID(), "2", "#22AA22"))
        otherRepository.save(Other(UUID.randomUUID(), "3", "#2222AA"))

        println(workingSetRepository.findAll())
    }
}

data class OtherIn(val name: String, val colour: String)

@MustBeDocumented
@Constraint(validatedBy = [ColorHexValidator::class])
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ColorHex(
    val message: String = "Invalid color hex code",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Component
class ColorHexValidator : ConstraintValidator<ColorHex, String> {
    override fun initialize(colorHexConstraint: ColorHex) {}
    override fun isValid(colorHexField: String, cxt: ConstraintValidatorContext): Boolean {
        return colorHexField.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))
    }
}

@MustBeDocumented
@Constraint(validatedBy = [UniquePrimaryValidator::class])
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.RUNTIME)
annotation class UniquePrimary(
    val message: String = "Invalid color hex code primary already exists",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Component
class HibernateValidatorCustomizer(private val provider: ObjectProvider<Validator>) : HibernatePropertiesCustomizer {
    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        val validator = provider.ifUnique
        if (validator != null) {
            hibernateProperties["javax.persistence.validation.factory"] = validator
        }
    }
}

@Component("beforeSaveOther")
class UniquePrimaryValidator(private val otherRepository: OtherRepository) : ConstraintValidator<UniquePrimary, String> {
    override fun initialize(uniquePrimaryConstraint: UniquePrimary) {}
    override fun isValid(colorHexField: String, cxt: ConstraintValidatorContext): Boolean {
        return !otherRepository.existsByName(colorHexField)
    }
}

@Entity
class Other(
    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue
    var id: UUID? = null,
    var name: String?,
    @ColorHex
    @UniquePrimary
    var colour: String?
)

@Entity
class WorkingSet (
    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue
    var id: UUID? = null,
    @Column
    var name: String? = null
)

@GraphQlRepository
@RepositoryRestResource(collectionResourceRel = "workingset", path = "workingset")
interface WorkingSetRepository : JpaRepository<WorkingSet, UUID>, JpaSpecificationExecutor<WorkingSet>,
    QuerydslPredicateExecutor<WorkingSet>

@GraphQlRepository
@RepositoryRestResource(collectionResourceRel = "other", path = "other")
interface OtherRepository : JpaRepository<Other, UUID>, JpaSpecificationExecutor<Other>,
    QuerydslPredicateExecutor<Other> {
    @Transactional(NOT_SUPPORTED)
    fun existsByName(name: String): Boolean
}
