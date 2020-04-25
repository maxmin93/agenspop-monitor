package net.bitnine.ag3.agensalert.config

import net.bitnine.ag3.agensalert.model.event.EventQryHandler
import net.bitnine.ag3.agensalert.model.user.UserHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.coRouter


@Configuration
@EnableR2dbcRepositories
class WebConfiguration {
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
        GET("/query/hello", qryHandler::hello)
        GET("/query", qryHandler::findAll)
        GET("/query/search", qryHandler::search)
        GET("/query/{id}", qryHandler::findQuery)
        POST("/query", qryHandler::addQuery)
        PUT("/query/{id}", qryHandler::updateQuery)
        DELETE("/query/{id}", qryHandler::deleteQuery)
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

