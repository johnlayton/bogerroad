package org.bogerroad

import com.zaxxer.hikari.HikariDataSource
import org.hibernate.annotations.GenericGenerator
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.sql.DataSource


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

@Configuration
class AppConfiguration {

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
        val logger: Logger by lazy { LoggerFactory.getLogger(AppConfiguration::class.java) }
    }
}

/*
@EnableConfigurationProperties(
    QuartzProperties::class
)
*/
@Configuration
class QuartzConfiguration {

    @Bean
    @ConfigurationProperties("quartz.datasource")
    fun quartzDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @QuartzDataSource
    fun quartzDataSource(quartzDataSourceProperties: DataSourceProperties): DataSource {

        AppConfiguration.logger.info("---------------------------------------------------------------------------------")
        AppConfiguration.logger.info("Create Quartz Datasource with URL {}", quartzDataSourceProperties.url)
        AppConfiguration.logger.info("---------------------------------------------------------------------------------")

        return quartzDataSourceProperties.initializeDataSourceBuilder().build()
    }

    companion object {
        val logger: Logger by lazy { LoggerFactory.getLogger(QuartzConfiguration::class.java) }
    }
}
