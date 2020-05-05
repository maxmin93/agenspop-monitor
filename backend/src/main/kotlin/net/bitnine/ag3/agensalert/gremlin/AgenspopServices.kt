package net.bitnine.ag3.agensalert.gremlin

import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class AgenspopService(private val client: AgenspopClient){

    suspend fun findDatasources() =
            client.findDatasources().awaitFirstOrNull()

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

}