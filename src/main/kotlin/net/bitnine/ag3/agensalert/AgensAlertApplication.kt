package net.bitnine.ag3.agensalert

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AgensAlertApplication

fun main(args: Array<String>) {
    runApplication<AgensAlertApplication>(*args)
}

// ** Reference
// https://www.baeldung.com/spring-boot-kotlin-coroutines
// https://github.com/razvn/webflux-r2dbc-kotlin
// https://r2dbc.io/