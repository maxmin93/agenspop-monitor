package net.bitnine.ag3.agensalert

import net.bitnine.ag3.agensalert.user.UserHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.web.reactive.function.server.coRouter

@Configuration
@EnableR2dbcRepositories
class WebConfiguration {
    @Bean
    fun userRoute(userHandler: UserHandler) = coRouter {
        GET("/users", userHandler::findAll)
        GET("/users/search", userHandler::search)
        GET("/users/{id}", userHandler::findUser)
        POST("/users", userHandler::addUser)
        PUT("/users/{id}", userHandler::updateUser)
        DELETE("/users/{id}", userHandler::deleteUser)
    }
}
