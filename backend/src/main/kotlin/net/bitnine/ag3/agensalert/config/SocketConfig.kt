package net.bitnine.ag3.agensalert.config

import net.bitnine.ag3.agensalert.handler.PrimeNumbersHandler

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

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


// **NOTE
// WebSocketHandler 방식으로는 Login 처리하기가 어려움 (예제가 안보임)
// - reactive 방식이라 Event 단위의 상태 기술은 어떻게?
// - 세션의 Attributes 들을 통채로 넘겨받아 필요한 부분을 확인하도록 했다는데.. 호환성 있는 코드같지는 않음
// https://medium.com/@circlee7/spring-webflux-websocketsession%EC%9C%BC%EB%A1%9C-http-handshake-request-%EC%A0%95%EB%B3%B4-%EB%84%98%EA%B8%B0%EA%B8%B0-e006701a6481

@Configuration
@EnableWebSocketMessageBroker
class SimpleWebSocketConfig: WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }
}