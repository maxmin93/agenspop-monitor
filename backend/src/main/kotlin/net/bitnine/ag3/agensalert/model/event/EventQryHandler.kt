package net.bitnine.ag3.agensalert.model.event

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.bitnine.ag3.agensalert.model.user.ErrorMessage
import net.bitnine.ag3.agensalert.model.user.UserDTO

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*

@Component
class EventQryHandler(val service: EventQryService) {
    private val logger = LoggerFactory.getLogger(EventQryHandler::class.java)

    suspend fun hello(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().json().bodyAndAwait(flowOf("Hello, EventQryHandler!"))    //mapOf("msg" to "Hello, Spring!")))
    }

    suspend fun findAll(request: ServerRequest): ServerResponse {
        val qrys = service.findAll()
        return ServerResponse.ok().json().bodyAndAwait(qrys)
    }

    // by datasource
    suspend fun search(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Search must have query params"))
            criterias.contains("datasource") -> {
                val criteriaValue = criterias.getFirst("datasource")
                if (criteriaValue.isNullOrBlank()) {
                    ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Incorrect search criteria value"))
                } else {
                    ServerResponse.ok().json().bodyAndAwait(service.findByDatasource(criteriaValue))
                }
            }
            else -> ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Incorrect search criteria"))
        }
    }

    suspend fun findQuery(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val qry = service.findById(id)
            if (qry == null) ServerResponse.notFound().buildAndAwait()
            else ServerResponse.ok().json().bodyValueAndAwait(qry)
        }
    }

    suspend fun addQuery(request: ServerRequest): ServerResponse {
        val newQry = try {
            request.bodyToMono<EventQry>().awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error("Decoding body error", e)
            null
        }
        return if (newQry == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Invalid body"))
        } else {
            val qry = service.addOne(newQry)
            if (qry == null) ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).json().bodyValueAndAwait(ErrorMessage("Internal error"))
            else ServerResponse.status(HttpStatus.CREATED).json().bodyValueAndAwait(qry)
        }
    }

    suspend fun updateQuery(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val updateQry = try {
                request.bodyToMono<EventQry>().awaitFirstOrNull()
            } catch (e: Exception) {
                logger.error("Decoding body error", e)
                null
            }
            if (updateQry == null) {
                ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Invalid body"))
            } else {
                val qry = service.updateOne(id, updateQry)
                if (qry == null) ServerResponse.status(HttpStatus.NOT_FOUND).json().bodyValueAndAwait(ErrorMessage("Resource $id not found"))
                else ServerResponse.status(HttpStatus.OK).json().bodyValueAndAwait(qry)
            }
        }
    }

    suspend fun deleteQuery(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            if (service.deleteOne(id)) ServerResponse.noContent().buildAndAwait()
            else ServerResponse.status(HttpStatus.NOT_FOUND).json().bodyValueAndAwait(ErrorMessage("Resource $id not found"))
        }
    }
}