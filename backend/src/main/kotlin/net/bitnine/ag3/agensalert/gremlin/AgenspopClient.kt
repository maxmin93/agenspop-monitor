package net.bitnine.ag3.agensalert.gremlin

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

// reactor-extra since 3.1.1
import reactor.kotlin.extra.retry.retryExponentialBackoff

@Component
class AgenspopClient(private val webClient: WebClient){

    fun findDatasources() =
            webClient.get()
                    .uri("/admin/graphs")
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun findConnectedEdges(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/e/connected")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun findConnectedVertices(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/v/connected")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun findNeighborsOfOne(datasource:String, vid:String) =
            webClient.get()
                    .uri("/search/"+datasource+"/v/neighbors?q="+vid)
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun findNeighborsOfGrp(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/v/neighbors")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun findVertices(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/v/ids")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun findEdges(datasource:String, ids:List<String>) =
            webClient.post()
                    .uri("/search/"+datasource+"/e/ids")
                    .body(Mono.just(mapOf("q" to ids)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun execGremlin(datasource:String, script:String) =
            webClient.post()
                    .uri("/graph/gremlin")
                    .body(Mono.just(mapOf("datasource" to datasource, "q" to script)), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun execGremlin(datasource:String, script:String, fromDate:String, toDate:String?=null) =
            webClient.post()
                    .uri("/graph/gremlin")
                    .body(Mono.just(
                            mapOf("datasource" to datasource, "q" to script
                            , "from" to fromDate, "to" to toDate
                            )), Map::class.java)
                    .retrieve()
                    .bodyToFlux(Map::class.java)
                    .retryExponentialBackoff(3, Duration.ofMillis(200))

    fun findVerticesWithDateRange(ids:List<String>, fromDate: String, toDate: String?): Flux<Map<*, *>> {
        val params = mutableMapOf<String, Any?>("q" to ids, "from" to fromDate)
        if( toDate.isNullOrBlank().not() ) params.set("to", toDate)
        return webClient.post()
                .uri("/search/v/ids/date")
                .body(Mono.just(params), Map::class.java)
                .retrieve()
                .bodyToFlux(Map::class.java)
                .retryExponentialBackoff(3, Duration.ofMillis(200))
    }

    fun findEdgesWithDateRange(ids:List<String>, fromDate: String, toDate: String?): Flux<Map<*, *>> {
        val params = mutableMapOf<String, Any?>("q" to ids, "from" to fromDate)
        if (toDate.isNullOrBlank().not()) params.set("to", toDate)
        return webClient.post()
                .uri("/search/e/ids/date")
                .body(Mono.just(params), Map::class.java)
                .retrieve()
                .bodyToFlux(Map::class.java)
                .retryExponentialBackoff(3, Duration.ofMillis(200))
    }

    fun findElementsWithDateRange(ids:List<String>, fromDate: String, toDate: String?) =
            Flux.concat(
                    findVerticesWithDateRange(ids, fromDate, toDate),
                    findEdgesWithDateRange(ids, fromDate, toDate)
            )
}