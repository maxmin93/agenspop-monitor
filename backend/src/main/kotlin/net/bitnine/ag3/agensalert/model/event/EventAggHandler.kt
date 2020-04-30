package net.bitnine.ag3.agensalert.model.event

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.bitnine.ag3.agensalert.model.user.ErrorMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class EventAggHandler(@Autowired val service: EventAggService) {
    private val logger = LoggerFactory.getLogger(EventRowHandler::class.java)

    suspend fun hello(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().json().bodyAndAwait(flowOf("{ \"msg\": \"Hello, EventAggHandler!\""))    //mapOf("msg" to "Hello, Spring!")))
    }

    suspend fun findAll(request: ServerRequest): ServerResponse {
        val rows = service.findAll()
        return ServerResponse.ok().json().bodyAndAwait(rows)
    }

    // by datasource
    suspend fun search(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("Search must have query params"))
            criterias.contains("from") -> {
                val from = criterias.getFirst("from")
                if (from.isNullOrBlank()) {
                    ServerResponse.badRequest().json().bodyValueAndAwait(
                            ErrorMessage("Incorrect search criteria value"))
                }
                else {
                    val format: String? =if( criterias.contains("format") ) criterias.getFirst("format") else null
                    val to: String? = if( criterias.contains("to") ) criterias.getFirst("to") else null

                    val rows = if( !format.isNullOrBlank() ){
                        val formatter = DateTimeFormatter.ofPattern(format)
                        service.findByDateTerms( LocalDate.parse(from, formatter),
                                if( !to.isNullOrBlank()) LocalDate.parse(to, formatter) else null)
                    }
                    else{
                        service.findByDateTerms( LocalDate.parse(from, DateTimeFormatter.ISO_DATE),
                                if( !to.isNullOrBlank()) LocalDate.parse(to, DateTimeFormatter.ISO_DATE) else null)
                    }
                    ServerResponse.ok().json().bodyAndAwait(rows)
                }
            }
            else -> ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("Incorrect search criteria"))
        }
    }

    suspend fun findByQid(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("qid").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`qid` must be numeric"))
        } else {
            val rows = service.findByQid(id)
            ServerResponse.ok().json().bodyAndAwait(rows)
        }
    }

    suspend fun findOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val row = service.findById(id)
            if (row == null) ServerResponse.notFound().buildAndAwait()
            else ServerResponse.ok().json().bodyValueAndAwait(row)
        }
    }

    suspend fun addOne(request: ServerRequest): ServerResponse {
        val newRow = try {
            request.bodyToMono<EventAgg>().awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error("Decoding body error", e)
            null
        }
        return if (newRow == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Invalid body"))
        } else {
            val row = service.addOne(newRow)
            if (row == null) ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).json().bodyValueAndAwait(ErrorMessage("Internal error"))
            else ServerResponse.status(HttpStatus.CREATED).json().bodyValueAndAwait(row)
        }
    }

    suspend fun updateOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("`id` must be numeric"))
        } else {
            val updateRow = try {
                request.bodyToMono<EventAgg>().awaitFirstOrNull()
            } catch (e: Exception) {
                logger.error("Decoding body error", e)
                null
            }
            if (updateRow == null) {
                ServerResponse.badRequest().json().bodyValueAndAwait(
                        ErrorMessage("Invalid body"))
            } else {
                val row = service.updateOne(id, updateRow)
                if (row == null) ServerResponse.status(HttpStatus.NOT_FOUND).json().bodyValueAndAwait(ErrorMessage("Resource $id not found"))
                else ServerResponse.status(HttpStatus.OK).json().bodyValueAndAwait(row)
            }
        }
    }

    suspend fun deleteOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("`id` must be numeric"))
        } else {
            if (service.deleteOne(id)) ServerResponse.noContent().buildAndAwait()
            else ServerResponse.status(HttpStatus.NOT_FOUND).json().bodyValueAndAwait(
                    ErrorMessage("Resource $id not found"))
        }
    }
}