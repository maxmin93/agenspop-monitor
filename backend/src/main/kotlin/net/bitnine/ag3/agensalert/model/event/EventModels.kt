package net.bitnine.ag3.agensalert.model.event

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

// data models
@Table("event_qry")
data class EventQry(
        @Id val id: Long? = null,
        @Column("delete_yn") val delete_yn: Boolean,
        @Column("active_yn") val active_yn: Boolean,
        @Column("datasource") val datasource: String,
        @Column("query") val query: String,
        @Column("cr_time") val cr_time: Timestamp,
        @Column("up_time") val up_time: Timestamp
)

// data models
@Table("event_row")
data class EventRow(
        @Id val id: Long? = null,
        @Column("qid") val qid: Long,
        @Column("type") val type: String,
        @Column("labels") val labels: Array<String>,
        @Column("ids") val ids: Array<String>,
        @Column("edate") val edate: Date,
        @Column("etime") val etime: Timestamp
)

// data models
@Table("event_stat")
data class EventStat(
        @Id val id: Long? = null,
        @Column("edate") val edate: Date,
        @Column("qid") val qid: Long = 0,
        @Column("type") val type: String,
        @Column("labels") val labels: Array<Array<String>>,
        @Column("row_cnt") val row_cnt: Long,
        @Column("ids_cnt") val ids_cnt: Long
)


// rest models
data class EventErrorMessage(val message: String)

data class EventDTO(
        val edate: Date,
        val qid: Long,
        val type: String,
        val labels: List<String>,
        val ids_cnt: Long = 0
)

fun EventStat.toDTO():EventDTO = run {
    val labels:List<String> = this.labels.toList().map {
        if(it is Array) it.toList()
        else listOf(it.toString())
    }.flatten()

    EventDTO(this.edate, this.qid, this.type, labels, this.ids_cnt)
}
