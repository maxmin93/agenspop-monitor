package net.bitnine.ag3.agensalert.config

import net.bitnine.ag3.agensalert.gremlin.AgenspopHandler
import net.bitnine.ag3.agensalert.event.EventAggHandler
import net.bitnine.ag3.agensalert.event.EventQryHandler
import net.bitnine.ag3.agensalert.event.EventRowHandler
import net.bitnine.ag3.agensalert.storage.H2AdminHandler

import org.h2.tools.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.resources
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.router
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.URI
import java.sql.SQLException


@Configuration
@EnableR2dbcRepositories
class WebApiConfiguration(private val properties: MonitorProperties) {

    // H2 WebConsole
    // url => http://localhost:<h2ConsolePort>

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Throws(SQLException::class)
    fun h2WebConsoleServer(): Server {
        return Server.createWebServer("-web", "-webAllowOthers", "-webDaemon", "-webPort", properties.h2ConsolePort)
    }

    // **NOTE : webflux-functional
    // https://github.com/spring-projects/spring-framework/blob/master/src/docs/asciidoc/web/webflux-functional.adoc#serverresponse

    @Bean
    fun agenspopRoute(agenspopHandler: AgenspopHandler) = coRouter {
        GET("/agens/hello", agenspopHandler::hello)
        GET("/agens/datasources", agenspopHandler::findDatasources)
        GET("/agens/vertices", agenspopHandler::findVertices)
        GET("/agens/edges", agenspopHandler::findEdges)
        GET("/agens/neighbors", agenspopHandler::findNeighborsOfOne)
        POST("/agens/neighbors", agenspopHandler::findNeighborsOfGrp)
        POST("/agens/connected_edges", agenspopHandler::findConnectedEdges)
        POST("/agens/connected_vertices", agenspopHandler::findConnectedVertices)
        POST("/agens/elements", agenspopHandler::findElements)
        POST("/agens/gremlin", agenspopHandler::execGremlin)
        POST("/agens/gremlin/range", agenspopHandler::execGremlinWithRange)
        POST("/agens/ids/range", agenspopHandler::findIdsWithTimeRange)
    }

    @Bean
    fun eventQryRoute(qryHandler: EventQryHandler) = coRouter {
        GET("/query/hello", qryHandler::hello)
        GET("/query/all", qryHandler::findAllWithDeleted)
        GET("/query", qryHandler::findAll)
        GET("/query/{id}/change-state", qryHandler::changeStateOne)
        GET("/query/search", qryHandler::search)
        GET("/query/{id}/date-range", qryHandler::findDateRange)
        GET("/query/{id}", qryHandler::findOne)
        POST("/query", qryHandler::addOne)
        PUT("/query/{id}", qryHandler::updateOne)
//        DELETE("/queries/{id}", qryHandler::removeOne)
    }

    @Bean
    fun eventRowRoute(rowHandler: EventRowHandler) = coRouter {
        GET("/rows/hello", rowHandler::hello)
        GET("/rows", rowHandler::findAll)
        GET("/rows/qid/{qid}", rowHandler::findByQid)        // date, time
        GET("/rows/search/date", rowHandler::searchDate)     // qid, from, to
        GET("/rows/search/time", rowHandler::searchTime)     // qid, from, to
        GET("/rows/{id}", rowHandler::findOne)
        POST("/rows", rowHandler::addOne)
        PUT("/rows/{id}", rowHandler::updateOne)
//        DELETE("/rows/{id}", rowHandler::deleteOne)
    }

    @Bean
    fun eventAggRoute(aggHandler: EventAggHandler) = coRouter {
        GET("/aggs/hello", aggHandler::hello)
        GET("/aggs", aggHandler::findAll)
        GET("/aggs/search", aggHandler::search)
        GET("/aggs/qid/{qid}", aggHandler::findByQid)
        GET("/aggs/{id}", aggHandler::findOne)
        POST("/aggs", aggHandler::addOne)
        PUT("/aggs/{id}", aggHandler::updateOne)
//        DELETE("/aggs/{id}", aggHandler::deleteOne)
    }

    @Bean
    fun storageAdminRoute(adminHandler: H2AdminHandler) = coRouter {
        GET("/admin/hello", adminHandler::hello)
        GET("/admin/activate", adminHandler::changeState)
        GET("/admin/batch/all", adminHandler::doBatchAll)
        GET("/admin/realtime/reset", adminHandler::doRealtimeReset)
        GET("/admin/realtime/test", adminHandler::doRealtimeTest)
    }

    /////////////////////////////////
    // frontend
    // https://medium.com/@davidecerbo/serving-static-resources-with-spring-webflux-and-kotlin-ad831cbd26eb

    @Bean
    fun resRouter() = resources("/**", ClassPathResource("static/"))

    @Bean
    fun indexRouter(@Value("classpath:/static/index.html") html:
                    Resource) = router {
        GET("/") {
            ok().contentType(TEXT_HTML).syncBody(html)
        }
        GET("/monitor") {
            ok().contentType(TEXT_HTML).syncBody(html)
        }
    }
}

///////////////////////////////////////////////////////

@Component
class CorsFilter : WebFilter {
    override fun filter(ctx: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (ctx != null) {
            ctx.response.headers.add("Access-Control-Allow-Origin", "*")
            ctx.response.headers.add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE")
            ctx.response.headers.add("Access-Control-Allow-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range")
            if (ctx.request.method == HttpMethod.OPTIONS) {
                ctx.response.headers.add("Access-Control-Max-Age", "1728000")
                ctx.response.statusCode = HttpStatus.NO_CONTENT
                return Mono.empty()
            } else {
                ctx.response.headers.add("Access-Control-Expose-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range")
                return chain?.filter(ctx) ?: Mono.empty()
            }
        } else {
            return chain?.filter(ctx) ?: Mono.empty()
        }
    }
}
