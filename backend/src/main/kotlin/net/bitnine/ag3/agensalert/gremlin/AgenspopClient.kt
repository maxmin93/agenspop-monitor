package net.bitnine.ag3.agensalert.gremlin

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class AgenspopClient(private val webClient: WebClient){

    fun findDatasources() =
            webClient.get()
                    .uri("/admin/graphs")
                    .retrieve()
                    .bodyToMono(Map::class.java)

    fun findConnectedEdges(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/e/connected")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)

    fun findConnectedVertices(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/v/connected")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)

    fun findNeighborsOfOne(datasource:String, vid:String) =
            webClient.get()
                    .uri("/search/"+datasource+"/v/neighbors?q="+vid)
                    .retrieve()
                    .bodyToMono(Map::class.java)

    fun findNeighborsOfGrp(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/v/neighbors")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)

    fun findVertices(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/v/ids")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)

    fun findEdges(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/e/ids")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)

    fun execGremlin(datasource:String, script:String) =
            webClient.post()
                    .uri("/graph/gremlin")
                    .body(Mono.just(mapOf("datasource" to datasource, "q" to script)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)

    fun execGremlin(datasource:String, script:String, fromDate:String, toDate:String?=null) =
            webClient.post()
                    .uri("/graph/gremlin")
                    .body(Mono.just(
                            mapOf("datasource" to datasource, "q" to script
                            , "from" to fromDate, "to" to toDate
                            )), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)

}