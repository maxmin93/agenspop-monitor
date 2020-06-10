package net.bitnine.ag3.agensalert.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class WebClientConfiguration(
        private val properties: MonitorProperties
){

    @Bean
    @Primary
    fun webClientDefault() =
            WebClient.builder()
                    .baseUrl(properties.baseUri)
                    .build()

}