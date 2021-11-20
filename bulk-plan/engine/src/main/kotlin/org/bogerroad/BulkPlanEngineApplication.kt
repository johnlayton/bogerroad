package org.bogerroad

import com.fasterxml.jackson.databind.ObjectMapper
import net.logstash.logback.argument.StructuredArguments
import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.lookup.PlanningId
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.domain.variable.PlanningVariable
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.api.score.stream.Constraint
import org.optaplanner.core.api.score.stream.ConstraintFactory
import org.optaplanner.core.api.score.stream.ConstraintProvider
import org.optaplanner.core.api.score.stream.Joiners
import org.optaplanner.core.api.solver.SolverManager
import org.optaplanner.core.api.solver.SolverStatus
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
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.JsonPath
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
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

@ConfigurationProperties(prefix = "engine")
class EngineProperties {
    var routing: String = ""
}

@Configuration
@EnableConfigurationProperties(
    EngineProperties::class
)
class EngineConfiguration(val engineProperties: EngineProperties) {

    @Bean
    fun routing(): String = engineProperties.routing

    companion object {
        private val logger = LoggerFactory.getLogger(EngineConfiguration::class.java)
    }

    init {
        logger.info("=======================================")
        logger.info("Create Engine Configuration")
        logger.info("=======================================")
    }
}


@Configuration
class RabbitConfiguration(
    val amqpAdmin: AmqpAdmin,
    val engineProperties: EngineProperties
) {
//    private val LOGGER = LoggerFactory.getLogger(javaClass)
//    private val routing : String = UUID.randomUUID().toString()

    companion object {
        private val logger = LoggerFactory.getLogger(RabbitConfiguration::class.java)
    }

    @Bean
    fun classMapper(): DefaultClassMapper {
        return DefaultClassMapper().also { mapper ->
            mapper.setIdClassMapping(
                mapOf(
                    "createPlan" to CreatePlan::class.java,
                    "createdPlan" to CreatedPlan::class.java,
                    "generated" to TimeTable::class.java
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
                StructuredArguments.v("routingKey", engineProperties.routing)
            )

            val ex =
                ExchangeBuilder.directExchange("planning")
                    .durable(true)
                    .build<Exchange>()
            amqpAdmin.declareExchange(ex)

            val control = QueueBuilder.durable("${engineProperties.routing}-control")
                .autoDelete()
                .build()
            amqpAdmin.declareQueue(control)

            val generated = QueueBuilder.durable("${engineProperties.routing}-generated")
                .autoDelete()
                .build()
            amqpAdmin.declareQueue(generated)

            amqpAdmin.declareBinding(
                BindingBuilder
                    .bind(control)
                    .to(ex)
                    .with("${engineProperties.routing}-control")
                    .noargs()
            )

            amqpAdmin.declareBinding(
                BindingBuilder
                    .bind(generated)
                    .to(ex)
                    .with("${engineProperties.routing}-generated")
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

interface StopPlanProjection {
    @get:JsonPath("$.reference")
    val reference: String
}

@Component
class RabbitComponent(
    private val amqpTemplate: AmqpTemplate,
    private val engineProperties: EngineProperties,
    private val planningService: PlanningService
) {

    //    private val logger = LoggerFactory.getLogger(javaClass)
    companion object {
        private val logger = LoggerFactory.getLogger(RabbitConfiguration::class.java)
    }

    /**
     * Use Spring Cloud and Bind as a Processor
     */
    @RabbitListener(queues = ["create"])
    fun createPlan(projection: CreatePlanProjection) {
        logger.info("=========================================================")
        logger.info("Plan Create:")
        logger.info("\tReference: {}", StructuredArguments.v("reference", projection.reference))
        logger.info("=========================================================")
        amqpTemplate.convertAndSend(
            "created", CreatedPlan(
                reference = projection.reference,
                routing = engineProperties.routing
            )
        )
        planningService.plan(projection.reference)
    }

    /**
     * Use Spring Cloud and Bind as a Sink
     */
    @RabbitListener(queues = ["#{engineConfiguration.routing}-control"])
    fun stopPlan(projection: StopPlanProjection) {
        logger.info("=========================================================")
        logger.info("Plan Stop:")
        logger.info("\tReference: {}", StructuredArguments.v("reference", projection.reference))
        logger.info("=========================================================")
    }
}

@Component
class PlanningService(
    val solverManager: SolverManager<TimeTable, String>,
    val amqpTemplate: AmqpTemplate,
    val engineProperties: EngineProperties
) {
    //    private val logger by logger()
    companion object {
        private val logger = LoggerFactory.getLogger(RabbitConfiguration::class.java)
    }

    fun plan(reference: String) {
        logger.info("=========================================================")
        logger.info("Plan:")
        logger.info("\tReference: {}", StructuredArguments.v("reference", reference))
        logger.info("=========================================================")

        solverManager.solveAndListen(
            reference,
            { _ -> TimeTable(timeslots(), rooms(), lessons()) },
            { timeTable ->
                logger.info("=====================================================")
                logger.info("Better Plan")
                logger.info(timeTable.toString())
                logger.info("=====================================================")
                amqpTemplate.convertAndSend("${engineProperties.routing}-generated", timeTable)
            }
        )
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

fun timeslots(): List<Timeslot> =
    (8..12).flatMap { hour ->
        listOf(
            Timeslot(0, DayOfWeek.MONDAY, LocalTime.of(hour, 30), LocalTime.of(hour + 1, 30)),
            Timeslot(1, DayOfWeek.TUESDAY, LocalTime.of(hour, 30), LocalTime.of(hour + 1, 30))
        )
    }

fun rooms(): List<Room> =
    (0..1L).map { id ->
        Room(id, "Room ${id}")
    }

fun lessons(): List<Lesson> = listOf(
    Lesson(0L, "Math", "A. Turing", "9th grade"),
    Lesson(1, "Math", "A. Turing", "9th grade"),
    Lesson(2, "Physics", "M. Curie", "9th grade"),
    Lesson(3, "Chemistry", "M. Curie", "9th grade"),
    Lesson(4, "Biology", "C. Darwin", "9th grade"),
    Lesson(5, "History", "I. Jones", "9th grade"),
    Lesson(6, "English", "I. Jones", "9th grade"),
    Lesson(7, "English", "I. Jones", "9th grade"),
    Lesson(8, "Spanish", "P. Cruz", "9th grade"),
    Lesson(9, "Spanish", "P. Cruz", "9th grade")
)

class Room {
    var id: Long? = null

    lateinit var name: String

    constructor()

    constructor(name: String) {
        this.name = name
    }

    constructor(id: Long?, name: String)
            : this(name) {
        this.id = id
    }

    override fun toString(): String = name
}

class Timeslot {
    var id: Long? = null

    lateinit var dayOfWeek: DayOfWeek
    lateinit var startTime: LocalTime
    lateinit var endTime: LocalTime

    constructor()

    constructor(dayOfWeek: DayOfWeek, startTime: LocalTime, endTime: LocalTime) {
        this.dayOfWeek = dayOfWeek
        this.startTime = startTime
        this.endTime = endTime
    }

    constructor(id: Long?, dayOfWeek: DayOfWeek, startTime: LocalTime, endTime: LocalTime)
            : this(dayOfWeek, startTime, endTime) {
        this.id = id
    }

    override fun toString(): String = "$dayOfWeek $startTime"
}

@PlanningEntity
class Lesson {
    @PlanningId
    var id: Long? = null

    lateinit var subject: String
    lateinit var teacher: String
    lateinit var studentGroup: String

    @PlanningVariable(valueRangeProviderRefs = ["timeslotRange"])
    var timeslot: Timeslot? = null

    @PlanningVariable(valueRangeProviderRefs = ["roomRange"])
    var room: Room? = null

    constructor()

    constructor(subject: String, teacher: String, studentGroup: String) {
        this.subject = subject.trim()
        this.teacher = teacher.trim()
        this.studentGroup = studentGroup.trim()
    }

    constructor(id: Long?, subject: String, teacher: String, studentGroup: String)
            : this(subject, teacher, studentGroup) {
        this.id = id
    }

    constructor(id: Long?, subject: String, teacher: String, studentGroup: String,
                timeslot: Timeslot?, room: Room?)
            : this(id, subject, teacher, studentGroup) {
        this.timeslot = timeslot
        this.room = room
    }

    override fun toString(): String = "$subject($id)"
}

@PlanningSolution
class TimeTable {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    lateinit var timeslotList: List<Timeslot>
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "roomRange")
    lateinit var roomList: List<Room>
    @PlanningEntityCollectionProperty
    lateinit var lessonList: List<Lesson>

    @PlanningScore
    var score: HardSoftScore? = null

    // Ignored by OptaPlanner, used by the UI to display solve or stop solving button
    var solverStatus: SolverStatus? = null

    // No-arg constructor required for OptaPlanner
    constructor() {}

    constructor(timeslotList: List<Timeslot>, roomList: List<Room>, lessonList: List<Lesson>) {
        this.timeslotList = timeslotList
        this.roomList = roomList
        this.lessonList = lessonList
    }

}

class TimeTableConstraintProvider : ConstraintProvider {

    override fun defineConstraints(constraintFactory: ConstraintFactory): Array<Constraint>? {
        return arrayOf(
            // Hard constraints
            roomConflict(constraintFactory),
            teacherConflict(constraintFactory),
            studentGroupConflict(constraintFactory),
            // Soft constraints
            teacherRoomStability(constraintFactory),
            teacherTimeEfficiency(constraintFactory),
            studentGroupSubjectVariety(constraintFactory)
        )
    }

    fun roomConflict(constraintFactory: ConstraintFactory): Constraint {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
            // Select each pair of 2 different lessons ...
            .fromUniquePair(
                Lesson::class.java,
                // ... in the same timeslot ...
                Joiners.equal(Lesson::timeslot),
                // ... in the same room ...
                Joiners.equal(Lesson::room)
            )
            // ... and penalize each pair with a hard weight.
            .penalize("Room conflict", HardSoftScore.ONE_HARD)
    }

    fun teacherConflict(constraintFactory: ConstraintFactory): Constraint {
        // A teacher can teach at most one lesson at the same time.
        return constraintFactory
            .fromUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::teacher)
            )
            .penalize("Teacher conflict", HardSoftScore.ONE_HARD)
    }

    fun studentGroupConflict(constraintFactory: ConstraintFactory): Constraint {
        // A student can attend at most one lesson at the same time.
        return constraintFactory
            .fromUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::studentGroup)
            )
            .penalize("Student group conflict", HardSoftScore.ONE_HARD)
    }

    fun teacherRoomStability(constraintFactory: ConstraintFactory): Constraint {
        // A teacher prefers to teach in a single room.
        return constraintFactory
            .fromUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::teacher)
            )
            .filter { lesson1: Lesson, lesson2: Lesson -> lesson1.room !== lesson2.room }
            .penalize("Teacher room stability", HardSoftScore.ONE_SOFT)
    }

    fun teacherTimeEfficiency(constraintFactory: ConstraintFactory): Constraint {
        // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
        return constraintFactory
            .from(Lesson::class.java)
            .join(Lesson::class.java, Joiners.equal(Lesson::teacher),
                Joiners.equal { lesson: Lesson -> lesson.timeslot?.dayOfWeek })
            .filter { lesson1: Lesson, lesson2: Lesson ->
                val between = Duration.between(
                    lesson1.timeslot?.endTime,
                    lesson2.timeslot?.startTime
                )
                !between.isNegative && between.compareTo(Duration.ofMinutes(30)) <= 0
            }
            .reward("Teacher time efficiency", HardSoftScore.ONE_SOFT)
    }

    fun studentGroupSubjectVariety(constraintFactory: ConstraintFactory): Constraint {
        // A student group dislikes sequential lessons on the same subject.
        return constraintFactory
            .from(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal(Lesson::subject),
                Joiners.equal(Lesson::studentGroup),
                Joiners.equal { lesson: Lesson -> lesson.timeslot?.dayOfWeek })
            .filter { lesson1: Lesson, lesson2: Lesson ->
                val between = Duration.between(
                    lesson1.timeslot?.endTime,
                    lesson2.timeslot?.startTime
                )
                !between.isNegative && between.compareTo(Duration.ofMinutes(30)) <= 0
            }
            .penalize("Student group subject variety", HardSoftScore.ONE_SOFT)
    }

}
