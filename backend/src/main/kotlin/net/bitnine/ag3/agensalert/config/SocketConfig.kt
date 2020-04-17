package net.bitnine.ag3.agensalert.config

import net.bitnine.ag3.agensalert.handler.PrimeNumbersHandler

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
@EnableWebFlux
@ComponentScan(value = ["net.bitnine.ag3.agensalert.handler"])
class ReactiveWebSocketConfig(
        val primeNumbersHandler: PrimeNumbersHandler
) {
    @Bean
    fun websocketHandlerAdapter() = WebSocketHandlerAdapter()

    // ** Connect URL (Simple WebSocket Client)
    // ws://localhost:8080/ws/primes

    @Bean
    fun handlerMapping() : HandlerMapping {
        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.urlMap = mapOf(
                "/ws/primes" to primeNumbersHandler
                )
        handlerMapping.order = 1
        handlerMapping.setCorsConfigurations(
                mapOf("*" to CorsConfiguration().applyPermitDefaultValues())
                )
        return handlerMapping
    }
}
