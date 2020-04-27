package net.bitnine.ag3.agensalert

import net.bitnine.ag3.agensalert.config.MonitorProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(MonitorProperties::class)
class AgensAlertApplication

fun main(args: Array<String>) {
    runApplication<AgensAlertApplication>(*args)
}

// ** Reference
// https://www.baeldung.com/spring-boot-kotlin-coroutines
// https://github.com/razvn/webflux-r2dbc-kotlin
// https://r2dbc.io/

// **NOTE
// DB handling 이 non-blocking 방식의 reactive 라는 것이지
// 그때문에 WebFlux 를 써야 되는건 아니지 않나?
// - 잦은 IO를 처리해야 하는 것은 실시간 그래프를 위한 WebSocket API 이고,
// - HTTP API 로는 전체 time-scrolling 에 대한 그래프 데이터셋의 dump 뿐

// **NOTE : 웹챗인데 이것은 참고만 할 것
// https://github.com/swsms/webchat
//
// - WebMVC 와 WebFlux 는 하나의 Application 에서 혼용이 안된다
// - 로그인 등의 복잡한 로직은 WebMVC 방식이 편하지만, 당장은 데모 목적이라 추가 안하기로
// - 새로 추가된 그래프만 WebSocket 으로 전달하는, 단순기능으로 간다
// - 급한 것은 Angular 쪽에 WebSocket 클라이언트와 Time-scrolling Chart
