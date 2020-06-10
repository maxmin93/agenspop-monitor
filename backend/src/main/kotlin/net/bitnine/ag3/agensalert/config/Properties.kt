package net.bitnine.ag3.agensalert.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


// **NOTE : used ConstructorBinding since Spring Boot 2.2.0
// https://stackoverflow.com/questions/45953118/kotlin-spring-boot-configurationproperties

@ConstructorBinding
@ConfigurationProperties(prefix = "agens.monitor")
data class MonitorProperties(
        val cronDaily: String = "0 5 1 * * ?",
        val cronRealtime: String = "*/30 * * * * ?",
        val baseUri: String,
        val h2ConsolePort: String = "8182"
){
    fun cronInterval() = cronRealtime.substring(
            cronRealtime.indexOf('/')+1,
            cronRealtime.indexOf('*',cronRealtime.indexOf('/')+2)
        ).trimEnd().toLong()
}

@ConstructorBinding
@ConfigurationProperties(prefix = "agens.product")
data class ProductProperties(
        val name: String = "alert",
        val version: String = "0.7.3-dev",
        val helloMsg: String = "Hello, agens-alert",
        val debug: Boolean = false
)
