package net.bitnine.ag3.agensalert.config

import net.bitnine.ag3.agensalert.model.event.EventAggHandler
import net.bitnine.ag3.agensalert.model.event.EventQryHandler
import net.bitnine.ag3.agensalert.model.event.EventRowHandler
import net.bitnine.ag3.agensalert.model.user.UserHandler
import org.h2.tools.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.coRouter
import java.sql.SQLException





@Configuration
@EnableR2dbcRepositories
class WebConfiguration {

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

    @Bean
    fun eventQryRoute(qryHandler: EventQryHandler) = coRouter {
        GET("/queries/hello", qryHandler::hello)
        GET("/queries", qryHandler::findAll)
        GET("/queries/search", qryHandler::search)
        GET("/queries/{id}", qryHandler::findOne)
        POST("/queries", qryHandler::addOne)
        PUT("/queries/{id}", qryHandler::updateOne)
        DELETE("/queries/{id}", qryHandler::deleteOne)
    }

    @Bean
    fun eventRowRoute(rowHandler: EventRowHandler) = coRouter {
        GET("/rows/hello", rowHandler::hello)
        GET("/rows", rowHandler::findAll)
        GET("/rows/search", rowHandler::search)
        GET("/rows/{id}", rowHandler::findOne)
        POST("/rows", rowHandler::addOne)
        PUT("/rows/{id}", rowHandler::updateOne)
        DELETE("/rows/{id}", rowHandler::deleteOne)
    }

    @Bean
    fun eventAggRoute(aggHandler: EventAggHandler) = coRouter {
        GET("/aggs/hello", aggHandler::hello)
        GET("/aggs", aggHandler::findAll)
        GET("/aggs/search", aggHandler::search)
        GET("/aggs/{id}", aggHandler::findOne)
        POST("/aggs", aggHandler::addOne)
        PUT("/aggs/{id}", aggHandler::updateOne)
        DELETE("/aggs/{id}", aggHandler::deleteOne)
    }
}

@Bean
fun corsFilter(): CorsWebFilter {

    val config = CorsConfiguration()

    // Possibly...
    // config.applyPermitDefaultValues()

    config.allowCredentials = true
    config.addAllowedOrigin("*")            // ("https://domain1.com")
    config.addAllowedHeader("*")
    config.addAllowedMethod("*")

    val source = UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration("/**", config)
    }
    return CorsWebFilter(source)
}

