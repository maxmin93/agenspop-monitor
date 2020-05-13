package net.bitnine.ag3.agensalert.model.event

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalTime


// **NOTE :
// Binding Values to Queries
// https://github.com/spring-projects/spring-data-r2dbc/blob/master/src/main/asciidoc/reference/r2dbc-sql.adoc
// ==> DATE 에 대한 bind 가 안됨 (통채로 문자열화 시켜 넣어야 작동) => IndexOutOfBoundsException
//      .bind("from", LocalDate.of(2019,1,1))  => DATE '2019-01-01'
//      DATE '2019-01-01'에서 'DATE' 없어도 괜찮음

// data models
@Table("event_qry")
data class EventQry(
        @Id val id: Long? = null,
        @Column("delete_yn") val delete_yn: Boolean = false,
        @Column("active_yn") val active_yn: Boolean = false,
        @Column("datasource") val datasource: String,
        @Column("name") val name: String,
        @Column("script") val script: String,                 // cannot be modified, only insert
        @JsonFormat(pattern="yyyy-MM-dd")
        @CreatedDate
        @Column("cr_date") val cr_date: LocalDate? = null,
        @JsonFormat(pattern="yyyy-MM-dd")
        @LastModifiedDate
        @Column("up_date") val up_date: LocalDate? = LocalDate.now()   // when deactivated or deleted
)

// Add support for LocalDate, LocalDateTime, and LocalTime types
// https://github.com/r2dbc/r2dbc-h2/issues/67

enum class EleType { nodes, edges }

// r2dbc : type mapping
// https://docs.spring.io/spring-data/r2dbc/docs/current-SNAPSHOT/reference/html/#mapping.types
// ==> 포기!! ArrayCodec 이 작동하지 않는다. jacksonMapper로 List를 String으로 변환
// https://github.com/r2dbc/r2dbc-h2/tree/master/src/main/java/io/r2dbc/h2/codecs

// data models
@Table("event_row")
data class EventRow(
        @Id val id: Long? = null,
        @Column("qid") val qid: Long,
        @Column("type") val type: String? = null,
//        @Column("labels") val labels: Array<Any>? = null,
//        @Column("ids") val ids: Array<Any>? = null,
        @Column("labels") val labels: String? = null,
        @Column("ids") val ids: String? = null,
        @Column("ids_cnt") val ids_cnt: Long = 0L,
        @JsonFormat(pattern="yyyy-MM-dd")
        @Column("edate") val edate: LocalDate? = null,
        @JsonFormat(pattern="HH:mm:ss")
        @Column("etime") val etime: LocalTime? = null
)

// data models
@Table("event_agg")
data class EventAgg(
        @Id val id: Long? = null,
        @JsonFormat(pattern="yyyy-MM-dd")
        @Column("edate") val edate: LocalDate,
        @Column("qid") val qid: Long = 0,
        @Column("type") val type: String? = null,
//        @Column("labels") val labels: Array<Array<String>>,
        @Column("labels") val labels: String? = null,
        @Column("row_cnt") val row_cnt: Long,
        @Column("ids_cnt") val ids_cnt: Long
)


class EventDateRange(
        val from_date: LocalDate,
        val to_date: LocalDate,
        val cnt: Long
)

// rest models
data class EventErrorMessage(val message: String)

data class EventUpdateMessage(
        val qid: Long,
        val result: Any
)

data class EventDTO(
        val edate: LocalDate,
        val qid: Long,
        val type: String? = null,
        val labels: List<String>,
        val ids_cnt: Long = 0
)

fun EventAgg.toDTO():EventDTO = run {
    val mapper = jacksonObjectMapper()
    val reader = mapper.readerFor(object: TypeReference<List<String>>(){})
    val labels:List<String> = reader.readValue(labels)

    EventDTO(this.edate, this.qid, this.type, labels, this.ids_cnt)
}
