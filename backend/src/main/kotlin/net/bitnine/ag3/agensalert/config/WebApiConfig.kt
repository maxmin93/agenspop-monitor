package net.bitnine.ag3.agensalert.config

import net.bitnine.ag3.agensalert.gremlin.AgenspopHandler
import net.bitnine.ag3.agensalert.model.event.EventAggHandler
import net.bitnine.ag3.agensalert.model.event.EventQryHandler
import net.bitnine.ag3.agensalert.model.event.EventRowHandler
import net.bitnine.ag3.agensalert.model.user.UserHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableR2dbcRepositories
class WebApiConfiguration {

//    @Bean(initMethod = "start", destroyMethod = "stop")
//    @Throws(SQLException::class)
//    fun h2WebConsoleServer(): Server {
//        return Server.createWebServer("-web", "-webAllowOthers", "-webDaemon")
//    }

    @Bean
    fun userRoute(userHandler: UserHandler) = coRouter {
        GET("/users/hello", userHandler::hello)
        GET("/users", userHandler::findAll)
        GET("/users/search", userHandler::search)
        GET("/users/{id}", userHandler::findUser)
        POST("/users", userHandler::addUser)
        PUT("/users/{id}", userHandler::updateUser)
        DELETE("/users/{id}", userHandler::deleteUser)
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
    }

    @Bean
    fun eventQryRoute(qryHandler: EventQryHandler) = coRouter {
        GET("/queries/hello", qryHandler::hello)
        GET("/queries/all", qryHandler::findAllWithDeleted)
        GET("/queries", qryHandler::findAll)
        GET("/queries/{id}/changeState", qryHandler::changeStateOne)
        GET("/queries/search", qryHandler::search)
        GET("/queries/{id}", qryHandler::findOne)
        POST("/queries", qryHandler::addOne)
        PUT("/queries/{id}", qryHandler::updateOne)
//        DELETE("/queries/{id}", qryHandler::removeOne)
    }

    @Bean
    fun eventRowRoute(rowHandler: EventRowHandler) = coRouter {
        GET("/rows/hello", rowHandler::hello)
        GET("/rows", rowHandler::findAll)
        GET("/rows/search", rowHandler::search)
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
}


@Component
class CorsFilter : WebFilter {
    // Mono<Void> filter(ServerWebExchange var1, WebFilterChain var2);
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
