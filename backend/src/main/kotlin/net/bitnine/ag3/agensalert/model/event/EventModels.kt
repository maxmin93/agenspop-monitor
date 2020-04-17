package net.bitnine.ag3.agensalert.model.event

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

// data models
@Table("events")
data class Event(
        @Id val id: Long? = null,
        @Column("qid") val qid: Long,
        @Column("datasource") val login: String,
        @Column("group") val type: String,           // nodes, edges
        @Column("ids") val ids: Array<String>,
        @Column("etime") val etime: Date = Date()
)


// rest models
data class EventErrorMessage(val message: String)

data class EventDTO(
        val name: String,
        val login: String,
        val email: String,
        val avatar: String? = null
)

fun EventDTO.toModel(withId: Long? = null) = Event(withId, this.name, this.login, this.email, this.avatar)
