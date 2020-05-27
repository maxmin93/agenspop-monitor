package net.bitnine.ag3.agensalert.storage

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.bitnine.ag3.agensalert.event.ErrorMessage
import net.bitnine.ag3.agensalert.gremlin.AgenspopUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.json
import java.time.LocalDate


@Component
class H2AdminHandler(@Autowired val service: H2SheduleService) {
    private val logger = LoggerFactory.getLogger(H2AdminHandler::class.java)

    suspend fun hello(request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait( mapOf("msg" to "Hello, H2AdminHandler!") )
    }

/*
http://localhost:8082/admin/activate?q=false
 */
    suspend fun changeState(request: ServerRequest): ServerResponse {
        val params = request.queryParams()
        val state = params.get("q")?.toString()!!.toLowerCase().equals("true") ?: false
        service.setActivate(state)

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait( mapOf("activated" to service.isActivate()) )
    }

/*
http://localhost:8082/admin/batch/all?from=2019-01-01
 */
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

/*
http://localhost:8082/admin/realtime/test?datasource=airroutes
 */
    suspend fun doRealtimeTest(request: ServerRequest): ServerResponse {
        val params = request.queryParams()

        // **NOTE: 왜 list of string 으로 받아오지?
        val datasource = params.get("datasource")?.first().toString()
        if( datasource.isNullOrBlank() ){
            return ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Incorrect parameter value: "
                            +"datasource"))
        }

        println("\ndoRealtimeTest to '${datasource}'___________")
        service.realtimeTest(datasource)

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait( mapOf("status" to service.isActivate()) )
    }

}
