package net.bitnine.ag3.agensalert.socket

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// datetime, qid, ids
data class EventDto(val sender: Int, val value: Int)

@Component
class EventPublishHandler: WebSocketHandler {

    private val processor = EmitterProcessor.create<EventDto>() //("shared", 1024)
    private val outputEvents = Flux.from(processor)

    // outputEvents 에 데이터를 태워서 보내면 되나?
    // broadcasting 하는 방법은?

    override fun handle(session: WebSocketSession): Mono<Void> {
        val output = session.send(
                outputEvents.map{ ev -> session.textMessage("${ev.sender}:${ev.value}") }
        )

        return output.then()
    }
}