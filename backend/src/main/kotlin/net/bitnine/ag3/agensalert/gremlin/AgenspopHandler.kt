package net.bitnine.ag3.agensalert.gremlin

import kotlinx.coroutines.flow.flowOf
import net.bitnine.ag3.agensalert.model.user.ErrorMessage
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
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait( mapOf("msg" to "Hello, AgenspopHandler!") )
    }

    suspend fun findDatasources(request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait(service.findDatasources()!!)
    }

/*
    suspend fun findNeighbors(datasource:String, vid:String) =
            client.findNeighbors(datasource, vid).awaitFirstOrNull()

    suspend fun findConnectedEdges(datasource:String, vids: List<String>) =
            client.findConnectedEdges(datasource, vids).asFlow()

    suspend fun findVertices(datasource:String, ids: List<String>) =
            client.findVertices(datasource, ids).asFlow()

    suspend fun findEdges(datasource:String, ids: List<String>) =
            client.findEdges(datasource, ids).asFlow()

    suspend fun findElements(datasource:String, ids: List<String>) =
            Flux.concat( client.findVertices(datasource, ids), client.findEdges(datasource, ids) ).asFlow()

 */
    suspend fun findNeighbors(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Search must have query params"))
            criterias.contains("datasource")&&criterias.contains("q") -> {
                val datasource = criterias.getFirst("datasource")
                val vid = criterias.getFirst("q")
                if (datasource.isNullOrBlank() || vid.isNullOrBlank()) {
                    ServerResponse.badRequest().json()
                            .bodyValueAndAwait(ErrorMessage("Incorrect search criteria value:"
                                    +"datasource, q"))
                } else {
                    ServerResponse.ok().json()
                            .bodyValueAndAwait(service.findNeighbors(datasource,vid)!!)
                }
            }
            else -> ServerResponse.badRequest().json()
                            .bodyValueAndAwait(ErrorMessage("Incorrect search criteria"))
        }
    }

    suspend fun findConnectedEdges(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Search must have query params"))
            criterias.contains("datasource")&&criterias.contains("q") -> {
                val datasource = criterias.getFirst("datasource")
                val vids:List<String>? = criterias.getFirst("q")?.split(",")
                if (datasource.isNullOrBlank() || vids.isNullOrEmpty()) {
                    ServerResponse.badRequest().json()
                            .bodyValueAndAwait(ErrorMessage("Incorrect search criteria value:"
                                    +"datasource, q"))
                } else {
                    ServerResponse.ok().json()
                            .bodyAndAwait(service.findConnectedEdges(datasource,vids)!!)
                }
            }
            else -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Incorrect search criteria"))
        }
    }

    suspend fun findVertices(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Search must have query params"))
            criterias.contains("datasource")&&criterias.contains("q") -> {
                val datasource = criterias.getFirst("datasource")
                val ids:List<String>? = criterias.getFirst("q")?.split(",")
                if (datasource.isNullOrBlank() || ids.isNullOrEmpty()) {
                    ServerResponse.badRequest().json()
                            .bodyValueAndAwait(ErrorMessage("Incorrect search criteria value:"
                                    +"datasource, q"))
                } else {
                    ServerResponse.ok().json()
                            .bodyAndAwait(service.findVertices(datasource,ids)!!)
                }
            }
            else -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Incorrect search criteria"))
        }
    }

    suspend fun findEdges(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Search must have query params"))
            criterias.contains("datasource")&&criterias.contains("q") -> {
                val datasource = criterias.getFirst("datasource")
                val ids:List<String>? = criterias.getFirst("q")?.split(",")
                if (datasource.isNullOrBlank() || ids.isNullOrEmpty()) {
                    ServerResponse.badRequest().json()
                            .bodyValueAndAwait(ErrorMessage("Incorrect search criteria value:"
                                    +"datasource, q"))
                } else {
                    ServerResponse.ok().json()
                            .bodyAndAwait(service.findEdges(datasource,ids)!!)
                }
            }
            else -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Incorrect search criteria"))
        }
    }

    suspend fun findElements(request: ServerRequest): ServerResponse {
        val criterias = request.queryParams()
        return when {
            criterias.isEmpty() -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Search must have query params"))
            criterias.contains("datasource")&&criterias.contains("q") -> {
                val datasource = criterias.getFirst("datasource")
                val ids:List<String>? = criterias.getFirst("q")?.split(",")
                if (datasource.isNullOrBlank() || ids.isNullOrEmpty()) {
                    ServerResponse.badRequest().json()
                            .bodyValueAndAwait(ErrorMessage("Incorrect search criteria value:"
                                    +"datasource, q"))
                } else {
                    ServerResponse.ok().json()
                            .bodyAndAwait(service.findElements(datasource,ids)!!)
                }
            }
            else -> ServerResponse.badRequest().json()
                    .bodyValueAndAwait(ErrorMessage("Incorrect search criteria"))
        }
    }

}
