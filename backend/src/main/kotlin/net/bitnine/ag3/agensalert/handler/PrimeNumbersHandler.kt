package net.bitnine.ag3.agensalert.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux

data class Event(val sender: Int, val value: Int)

@Component
class PrimeNumbersHandler : WebSocketHandler {
    private val processor = EmitterProcessor.create<Event>() //("shared", 1024)
    private val outputEvents = Flux.from(processor)

    // ** ref.
    // https://github.com/zupzup/kotlin-example-websockets/blob/master/src/main/kotlin/org/zupzup/kotlinwebfluxdemo/WebsocketHandler.kt
    // https://github.com/hiper2d/signaling-service/blob/master/src/main/kotlin/com/hiper2d/Application.kt

    override fun handle(session: WebSocketSession): Mono<Void> {
        val input = session.receive()
                .map { ev ->
                    val parts = ev.payloadAsText.split(":")
                    Event(sender = parts[0].toInt(), value = parts[1].toInt())
                }
                .filter { ev -> isPrime(ev.value) }
                .log()
                .doOnNext { ev -> processor.onNext(ev) }
                .then()

        val output = session.send(
                outputEvents.map{ ev -> session.textMessage("${ev.sender}:${ev.value}") }
        )

        return Mono.zip(input, output).then()
    }

    private fun isPrime(num: Int): Boolean {
        if (num < 2) return false
        if (num == 2) return true
        if (num % 2 == 0) return false
        var i = 3
        while (i * i <= num) {
            if (num % i == 0) return false
            i += 2
        }
        return true
    }
}