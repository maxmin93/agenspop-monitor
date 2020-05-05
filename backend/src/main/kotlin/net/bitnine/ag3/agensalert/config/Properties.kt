package net.bitnine.ag3.agensalert.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

// **NOTE : used ConstructorBinding since Spring Boot 2.2.0
// https://stackoverflow.com/questions/45953118/kotlin-spring-boot-configurationproperties

@ConstructorBinding
@ConfigurationProperties(prefix = "agens.monitor")
data class MonitorProperties(
        val cronExpression: String = "*/10 * * * * ?",
        val baseUri: String
)