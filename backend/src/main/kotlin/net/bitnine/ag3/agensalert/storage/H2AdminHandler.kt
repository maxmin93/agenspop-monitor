package net.bitnine.ag3.agensalert.storage

import net.bitnine.ag3.agensalert.event.ErrorMessage

import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.bitnine.ag3.agensalert.gremlin.AgenspopHandler
import net.bitnine.ag3.agensalert.gremlin.AgenspopService
import net.bitnine.ag3.agensalert.gremlin.AgenspopUtil

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.time.LocalDate


@Component
class H2AdminHandler(@Autowired val service: H2SheduleService) {
    private val logger = LoggerFactory.getLogger(H2AdminHandler::class.java)

    suspend fun hello(request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait( mapOf("msg" to "Hello, H2AdminHandler!") )
    }

    suspend fun changeState(request: ServerRequest): ServerResponse {
        val params = request.queryParams()
        val state = params.get("q")?.toString()!!.toLowerCase().equals("true") ?: false
        service.setActivate(state)

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait( mapOf("activated" to service.isActivate()) )
    }

    suspend fun doBatchAll(request: ServerRequest): ServerResponse {
        val params = request.queryParams()
        val fromValue = params.get("from")?.toString()
        var fromDate:LocalDate = LocalDate.now().minusYears(2)
        if( fromValue.isNullOrBlank().not() ){
            fromDate = AgenspopUtil.str2date(fromValue!!) ?: fromDate
        }

        println("\ndoBatchAll from '${fromValue}'___________")
        service.batchAll(fromDate)

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait( mapOf("activated" to service.isActivate()) )
    }

}