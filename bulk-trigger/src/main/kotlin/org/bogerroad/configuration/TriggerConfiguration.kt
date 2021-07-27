package org.bogerroad.configuration

import net.logstash.logback.argument.StructuredArguments.v
import org.bogerroad.TriggerJob
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.CronTrigger
import org.quartz.DateBuilder.IntervalUnit.MINUTE
import org.quartz.DateBuilder.futureDate
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.TriggerKey.triggerKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import java.util.TimeZone
import javax.annotation.PostConstruct


class ManagementTool

@Configuration
@EnableConfigurationProperties(
    TriggerProperties::class //, UploadProperties::class
)
class TriggerConfiguration(
    private val triggerProperties: TriggerProperties,
    private val schedulerFactoryBean: SchedulerFactoryBean
) {

    @Bean
    fun managementOfScheduler() : ManagementTool {
        return ManagementTool()
    }


//    @DataSource
    @Bean
    @QuartzDataSource
    fun quartzDataSource() {

    }

    @PostConstruct
    fun postConstruct() {
        triggerProperties.also { props ->
            logger.info("=========================================================")
            logger.info("Trigger Properties: {}", v("props", props))
            logger.info("=========================================================")
            props.triggers.forEach { trigger ->
                logger.info("\tCreate Trigger -> {} @ {}",
                    v("trigger_name", trigger.name),
                    v("trigger_cron", trigger.cron)
                )

                schedulerFactoryBean.scheduler.scheduleJob(
                    newJob(TriggerJob::class.java)
                        .withIdentity(trigger.name, trigger.name)
                        .usingJobData("title", trigger.name)
                        .build(),
                    newTrigger()
                        .withIdentity(triggerKey(trigger.name, trigger.name))
                        .withSchedule(cronSchedule(trigger.cron)
                            .inTimeZone(triggerProperties.timezone))
                        .startAt(futureDate(2, MINUTE))
                        .build()
                )

            }
        }
    }
    companion object {
        val logger: Logger by lazy {
            LoggerFactory.getLogger(TriggerConfiguration::class.java)
        }
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "trigger")
data class TriggerProperties(
    val name: String,
    val timezone: TimeZone,
    val triggers : List<UploadProperties>


) {
    @ConstructorBinding
    data class UploadProperties(
        val name: String,
        val cron: String,
        val time: String

    )
}

//@NestedConfigurationProperty
//@ConfigurationProperties

