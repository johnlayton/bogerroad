package org.bogerroad.configuration

import org.springframework.boot.autoconfigure.quartz.QuartzDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(
    QuartzProperties::class
)
class QuartzConfiguration {
    @Bean
//    @ConfigurationProperties("quartz.datasource")
    @QuartzDataSource
    fun quartzDataSource(quartzProperties: QuartzProperties): DataSource {
        return quartzProperties.datasource.initializeDataSourceBuilder()
            .type(SimpleDriverDataSource::class.java)
            .build();

//        return DataSourceBuilder.create()
////            .type(SimpleDriverDataSource::class.java)
//            .build()
    }
}

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
}



