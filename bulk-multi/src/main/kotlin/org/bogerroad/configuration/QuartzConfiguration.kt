package org.bogerroad.configuration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/*
@EnableConfigurationProperties(
    QuartzProperties::class
)
*/
@Configuration
class QuartzConfiguration {

    @Bean
    @ConfigurationProperties("spring.quartz.datasource")
    fun quartzDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @QuartzDataSource
    fun quartzDataSource(quartzDataSourceProperties: DataSourceProperties): DataSource {

        logger.info("---------------------------------------------------------------------------------")
        logger.info("Create Quartz Datasource with URL {}", quartzDataSourceProperties.url)
        logger.info("---------------------------------------------------------------------------------")

        return quartzDataSourceProperties.initializeDataSourceBuilder().build()
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(QuartzConfiguration::class.java) }
    }
}
