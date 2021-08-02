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
import org.springframework.boot.jdbc.DataSourceBuilder
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
    val id: String = UUID.randomUUID().toString(),
)

interface SettlementRepository : JpaRepository<Settlement, String>, JpaSpecificationExecutor<Settlement>

@Component
class SettlementLoader(private val repository: SettlementRepository) : CommandLineRunner {
    override fun run(vararg args: String?) {
        repository.save(Settlement())
    }
}

@Configuration
@EnableConfigurationProperties(
    QuartzProperties::class
)
class QuartzConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource")
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
//    @ConfigurationProperties("spring.datasource")
    fun dataSource(dataSourceProperties: DataSourceProperties): DataSource {
        return dataSourceProperties.initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()
    }

//    @Bean
//    @Primary
//    @ConfigurationProperties("spring.datasource")
//    fun dataSource(): DataSource {
//        return DataSourceBuilder.create().
//
//        build()
//    }

    @Bean
    @ConfigurationProperties("quartz.datasource")
    fun quartzDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @QuartzDataSource
//    @ConfigurationProperties("quartz.datasource")
    fun quartzDataSource(quartzDataSourceProperties: DataSourceProperties): DataSource {
        return quartzDataSourceProperties.initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()
//        return DataSourceBuilder.create().build()
//        return quartzProperties.datasource.initializeDataSourceBuilder()
//            .build();

//        return DataSourceBuilder.derivedFrom(dataSource)
//            .url(quartzProperties.datasource.url)
//            .username(quartzProperties.datasource.username)
//            .password(quartzProperties.datasource.password)
//            .build()
//            .type(SimpleDriverDataSource::class.java)
//        return DataSourceBuilder.create()
////            .type(SimpleDriverDataSource::class.java)
//            .build()
    }
}
//
@ConstructorBinding
@ConfigurationProperties(prefix = "quartz")
data class QuartzProperties(
    val datasource: QuartzConfigurationDataSource
) {
    @ConstructorBinding
    data class QuartzConfigurationDataSource(
        val driverClassName : String,
        val url : String,
        val username : String,
        val password : String
    )
/*
    ) {
        fun initializeDataSourceBuilder(): DataSourceBuilder<*> {
            return DataSourceBuilder
                .create()
//                .type(BasicDataSource::class.java)
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
        }
    }
*/
}
