package net.bitnine.ag3.agensalert.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "agens.monitor")
data class MonitorProperties(
        var cronExpression: String = "*/10 * * * * ?"
)