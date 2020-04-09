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

// **NOTE : 웹챗인데 이것은 참고만 할 것
// https://github.com/swsms/webchat
//
// - WebMVC 와 WebFlux 는 하나의 Application 에서 혼용이 안된다
// - 로그인 등의 복잡한 로직은 WebMVC 방식이 편하지만, 당장은 데모 목적이라 추가 안하기로
// - 새로 추가된 그래프만 WebSocket 으로 전달하는, 단순기능으로 간다
// - 급한 것은 Angular 쪽에 WebSocket 클라이언트와 Time-scrolling Chart

