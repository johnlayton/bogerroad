package org.bogerroad.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TriggerProperties::class)
class TriggerConfiguration

@ConstructorBinding
@ConfigurationProperties(prefix = "trigger")
data class TriggerProperties (
    val name: String
)
