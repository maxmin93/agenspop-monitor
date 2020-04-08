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
// https://github.com/swsms/webchat

// **NOTE
// DB handling 이 non-blocking 방식의 reactive 라는 것이지
// 그때문에 WebFlux 를 써야 되는건 아니지 않나?
// - 잦은 IO를 처리해야 하는 것은 실시간 그래프를 위한 WebSocket API 이고,
// - HTTP API 로는 전체 time-scrolling 에 대한 그래프 데이터셋의 dump 뿐
