package net.bitnine.ag3.agensalert.gremlin

import kotlinx.coroutines.flow.flowOf
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.EntityResponse.fromObject
import reactor.core.publisher.Mono

@Component
class AgenspopHandler(@Autowired val service: AgenspopService) {
    private val logger = LoggerFactory.getLogger(AgenspopHandler::class.java)

    suspend fun hello(request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
                .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait( mapOf("msg" to "Hello, AgenspopHandler!") )
    }

    suspend fun findDatasources(request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
                .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait(service.findDatasources()!!)
    }

}
