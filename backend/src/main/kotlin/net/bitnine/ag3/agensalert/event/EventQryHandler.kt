package net.bitnine.ag3.agensalert.event

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitFirstOrNull

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*


@Component
class EventQryHandler(@Autowired val service: EventQryService) {
    private val logger = LoggerFactory.getLogger(EventQryHandler::class.java)

    // **NOTE: SameSite=Lax Warning message
    // => Chrome 80 버전부터 반영.
    // https://yangbongsoo.gitbook.io/study/cookie-samesite

    suspend fun hello(request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
                .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                .json().bodyAndAwait( flowOf("{ \"msg\": \"Hello, EventQryHandler!\""))    //mapOf("msg" to "Hello, Spring!")))
    }

    suspend fun findAllWithDeleted(request: ServerRequest): ServerResponse {
        val rows = service.findAll()
        return ServerResponse.ok()
                .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                .json().bodyAndAwait(rows)
    }

    suspend fun findAll(request: ServerRequest): ServerResponse {
        val rows = service.findAllNotDeleted()
        return ServerResponse.ok()
                .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                .json().bodyAndAwait(rows)
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
                    ServerResponse.ok()
                            .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                            .json().bodyAndAwait(service.findByDatasource(criteriaValue))
                }
            }
            else -> ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Incorrect search criteria"))
        }
    }

    suspend fun findOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val row = service.findById(id)
            if (row == null) ServerResponse.notFound().buildAndAwait()
            else ServerResponse.ok()
                    .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                    .json().bodyValueAndAwait(row)
        }
    }

    suspend fun removeOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val row = service.removeById(id)
            if (row == null) ServerResponse.notFound().buildAndAwait()
            else ServerResponse.ok()
                    .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                    .json().bodyValueAndAwait(row)
        }
    }

    suspend fun changeStateOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()

        val criterias = request.queryParams()
        val state: Boolean? = if( criterias.contains("state") ) criterias.getFirst("state")?.toBoolean() else null

        return if (id == null || state == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric or `state` must be boolean"))
        } else {
            val row = service.changeStateById(id, state)
            if (row == null) ServerResponse.notFound().buildAndAwait()
            else ServerResponse.ok()
                    .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                    .json().bodyValueAndAwait( row )
        }
    }

    suspend fun addOne(request: ServerRequest): ServerResponse {
        val newRow = try {
            request.bodyToMono<EventQry>().awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error("Decoding body error", e)
            null
        }
        return if (newRow == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Invalid body"))
        } else {
            val row = service.addOne(newRow)
            if (row == null) ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).json().bodyValueAndAwait(ErrorMessage("Internal error"))
            else ServerResponse.status(HttpStatus.CREATED)
                    .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                    .json().bodyValueAndAwait(row)
        }
    }

    suspend fun updateOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val updateRow = try {
                request.bodyToMono<EventQry>().awaitFirstOrNull()
            } catch (e: Exception) {
                logger.error("Decoding body error", e)
                null
            }
            if (updateRow == null) {
                ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Invalid body"))
            } else {
                val row = service.updateOne(id, updateRow)
                if (row == null) ServerResponse.status(HttpStatus.NOT_FOUND).json().bodyValueAndAwait(ErrorMessage("Resource $id not found"))
                else ServerResponse.status(HttpStatus.OK)
                        .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                        .json().bodyValueAndAwait(row)
            }
        }
    }

    suspend fun deleteOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            if (service.deleteOne(id)) ServerResponse.noContent().buildAndAwait()
            else ServerResponse.status(HttpStatus.NOT_FOUND)
                    .header("Set-Cookie","HttpOnly;Secure;SameSite=Strict")
                    .json().bodyValueAndAwait(ErrorMessage("Resource $id not found"))
        }
    }
}