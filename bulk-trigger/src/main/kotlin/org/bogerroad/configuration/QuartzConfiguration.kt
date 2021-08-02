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
/*    @Bean(name = [ "quartzDataSource" ])
    @QuartzDataSource
    @ConfigurationProperties("quartz.datasource")
    fun quartzDataSource(): DataSource {
        return DataSourceBuilder.create().build()
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
    }*/
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



