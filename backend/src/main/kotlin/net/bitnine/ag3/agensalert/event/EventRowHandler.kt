package net.bitnine.ag3.agensalert.event

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitFirstOrNull

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Component
class EventRowHandler(@Autowired val service: EventRowService) {
    private val logger = LoggerFactory.getLogger(EventRowHandler::class.java)

    suspend fun hello(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().json().bodyAndAwait(flowOf("{ \"msg\": \"Hello, EventRowHandler!\""))    //mapOf("msg" to "Hello, Spring!")))
    }

    suspend fun findAll(request: ServerRequest): ServerResponse {
        val rows = service.findAll()
        return ServerResponse.ok().json().bodyAndAwait(rows)
    }

    // by datasource
    suspend fun searchDate(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("Search must have query params"))
            criterias.contains("qid") -> {
                val qid = criterias.getFirst("qid")
                val fromValue = criterias.getFirst("from")?.toString()
                val toValue = criterias.getFirst("to")?.toString()

                if (qid!!.toLongOrNull() == null || fromValue.isNullOrBlank()) {
                    ServerResponse.badRequest().json().bodyValueAndAwait(
                            ErrorMessage("Incorrect search criteria value: qid, from"))
                }
                else {
                    ServerResponse.ok().json().bodyAndAwait(
                            service.findEventsWithDateRange( qid.toLong(), fromValue, toValue)
                    )
                }
            }
            else -> ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("Incorrect search criteria"))
        }
    }

    // by datasource
    suspend fun searchTime(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("Search must have query params"))
            criterias.contains("qid") -> {
                val qid = criterias.getFirst("qid")
                val dateValue = criterias.getFirst("date")?.toString()
                val timeValue = criterias.getFirst("time")?.toString()

                if (qid!!.toLongOrNull() == null || dateValue.isNullOrBlank()) {
                    ServerResponse.badRequest().json().bodyValueAndAwait(
                            ErrorMessage("Incorrect search criteria value: qid, from"))
                }
                else {
                    ServerResponse.ok().json().bodyAndAwait(
                            service.findEventsWithTimeRange( qid.toLong(), dateValue, timeValue)
                    )
                }
            }
            else -> ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("Incorrect search criteria"))
        }
    }

    // by datasource
    suspend fun findByQid(request: ServerRequest): ServerResponse {
        val qid = request.pathVariable("qid").toLongOrNull()
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("Search must have query params"))
            criterias.contains("date") -> {
                val dateValue = criterias.getFirst("date")?.toString()
                val timeValue = criterias.getFirst("time")?.toString()

                if (qid == null || dateValue.isNullOrBlank()) {
                    ServerResponse.badRequest().json().bodyValueAndAwait(
                            ErrorMessage("Incorrect search criteria value: qid, fromDate"))
                }
                else {
                    ServerResponse.ok().json().bodyAndAwait(
                            service.findByQidWithTimeRange( qid, dateValue, timeValue)
                    )
                }
            }
            else -> ServerResponse.badRequest().json().bodyValueAndAwait(
                    ErrorMessage("Incorrect search criteria"))
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
            request.bodyToMono<EventRow>().awaitFirstOrNull()
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
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val updateRow = try {
                request.bodyToMono<EventRow>().awaitFirstOrNull()
            } catch (e: Exception) {
                logger.error("Decoding body error", e)
                null
            }
            if (updateRow == null) {
                ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Invalid body"))
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
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            if (service.deleteOne(id)) ServerResponse.noContent().buildAndAwait()
            else ServerResponse.status(HttpStatus.NOT_FOUND).json().bodyValueAndAwait(ErrorMessage("Resource $id not found"))
        }
    }
}