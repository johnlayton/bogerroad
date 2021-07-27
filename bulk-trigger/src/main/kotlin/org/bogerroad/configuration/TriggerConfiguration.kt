package org.bogerroad.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

@Configuration
@EnableConfigurationProperties(
    TriggerProperties::class //, UploadProperties::class
)
class TriggerConfiguration

@ConstructorBinding
@ConfigurationProperties(prefix = "trigger")
data class TriggerProperties(
    val name: String,
    val locale: TimeZone,
    val triggers : List<UploadProperties>
) {
    @ConstructorBinding
    data class UploadProperties(
        val name: String,
        val cron: String
    )
}
//    @NestedConfigurationProperty

//@ConfigurationProperties

