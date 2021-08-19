package org.bogerroad.configuration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class MultiConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource")
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    fun dataSource(dataSourceProperties: DataSourceProperties): DataSource {

        logger.info("---------------------------------------------------------------------------------")
        logger.info("Create App Datasource with URL {}", dataSourceProperties.url)
        logger.info("---------------------------------------------------------------------------------")

        return dataSourceProperties.initializeDataSourceBuilder().build()
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(MultiConfiguration::class.java) }
    }
}
