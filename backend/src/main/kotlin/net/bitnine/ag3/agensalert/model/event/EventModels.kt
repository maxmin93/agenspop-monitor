package net.bitnine.ag3.agensalert.model.event

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalTime

// data models
@Table("event_qry")
data class EventQry(
        @Id val id: Long? = null,
        @Column("delete_yn") val delete_yn: Boolean,
        @Column("active_yn") val active_yn: Boolean,
        @Column("datasource") val datasource: String,
        @Column("query") val query: String,                 // cannot be modified, only insert
        @Column("cr_date") val cr_date: LocalDate? = null,
        @Column("up_date") val up_date: LocalDate? = null   // when deactivated or deleted
)

// Add support for LocalDate, LocalDateTime, and LocalTime types
// https://github.com/r2dbc/r2dbc-h2/issues/67

enum class EleType { nodes, edges }

// r2dbc : type mapping
// https://docs.spring.io/spring-data/r2dbc/docs/current-SNAPSHOT/reference/html/#mapping.types
// ==> 포기!! ArrayCodec 이 작동하지 않는다. jacksonMapper로 List를 String으로 변환
// https://github.com/r2dbc/r2dbc-h2/tree/master/src/main/java/io/r2dbc/h2/codecs

// **NOTE: 이건 어떨까?
// R2dbcCustomConversions 에 Array 타입 등록해서 List로 Read/Write 한다면..
// https://medium.com/@nikola.babic1/mapping-to-json-fields-with-spring-data-r2dbc-and-reactive-postgres-driver-1db765067dc5

// data models
@Table("event_row")
data class EventRow(
        @Id val id: Long? = null,
        @Column("qid") val qid: Long,
        @Column("type") val type: String? = "nodes",
//        @Column("labels") val labels: Array<Any>? = null,
//        @Column("ids") val ids: Array<Any>? = null,
        @Column("labels") val labels: String? = null,
        @Column("ids") val ids: String? = null,
        @Column("edate") val edate: LocalDate? = null,
        @Column("etime") val etime: LocalTime? = null
)

// data models
@Table("event_agg")
data class EventAgg(
        @Id val id: Long? = null,
        @Column("edate") val edate: LocalDate,
        @Column("qid") val qid: Long = 0,
        @Column("type") val type: String,
//        @Column("labels") val labels: Array<Array<String>>,
        @Column("labels") val labels: String? = null,
        @Column("row_cnt") val row_cnt: Long,
        @Column("ids_cnt") val ids_cnt: Long
)


// rest models
data class EventErrorMessage(val message: String)

data class EventDTO(
        val edate: LocalDate,
        val qid: Long,
        val type: String,
        val labels: List<String>,
        val ids_cnt: Long = 0
)

fun EventAgg.toDTO():EventDTO = run {
    val mapper = jacksonObjectMapper()
    val reader = mapper.readerFor(object: TypeReference<List<String>>(){})
    val labels:List<String> = reader.readValue(labels)

    EventDTO(this.edate, this.qid, this.type, labels, this.ids_cnt)
}
