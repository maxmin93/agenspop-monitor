package net.bitnine.ag3.agensalert.model.event

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.*

interface EventQryRepository : ReactiveCrudRepository<EventQry, Long> {

    @Query("SELECT q.* FROM event_qry q WHERE q.id = :qid and q.delete_yn = false")
    fun findByQid(qid: Long): Mono<EventQry?>

    @Query("SELECT q.* FROM event_qry q WHERE q.delete_yn = false order by q.id")
    fun findAllNotDeleted(): Flux<EventQry>

    @Query("SELECT q.* FROM event_qry q WHERE q.datasource = :datasource and q.delete_yn = false")
    fun findByDatasource(datasource: String): Flux<EventQry>
}

interface EventRowRepository : ReactiveCrudRepository<EventRow, Long> {

    @Query("SELECT r.* FROM event_row r WHERE r.qid = :qid order by id")
    fun findByQid(qid: Long): Flux<EventRow>

    @Query("SELECT r.* FROM event_row r WHERE r.edate between :from and :to")
    fun findAllByDateTerms(from: LocalDate, to: LocalDate? = LocalDate.now()): Flux<EventRow>

    @Query("SELECT r.* FROM event_row r, event_qry q WHERE q.datasource = :datasource and q.id = r.qid order by r.edate, r.id")
    fun findAllByDatasource(datasource: String): Flux<EventRow>

//    @Id val id: Long? = null,
//    @Column("edate") val edate: LocalDate,
//    @Column("qid") val qid: Long = 0,
//    @Column("type") val type: EleType,
//    @Column("labels") val labels: Array<Array<String>>,
//    @Column("row_cnt") val row_cnt: Long,
//    @Column("ids_cnt") val ids_cnt: Long

    @Query("select TRANSACTION_ID() as id, edate, qid, type, " +
            "array_agg(labels) as labels, count(id) as row_cnt, sum(array_length(ids)) as ids_cnt\n" +
            "from event_row\n" +
            "where edate >= :from\n" +
            "group by edate, qid, type\n" +
            "order by edate, qid, type;")
    fun groupByEdateQid(from: LocalDate): Flux<EventAgg>
}

interface EventAggRepository : ReactiveCrudRepository<EventAgg, Long> {

    @Query("SELECT s.* FROM event_agg s WHERE s.qid = :qid order by id")
    fun findByQid(qid: Long): Flux<EventAgg>

    @Query("SELECT s.* FROM event_agg s WHERE s.edate between :from and :to")
    fun findAllByDateTerms(from: LocalDate, to: LocalDate? = LocalDate.now()): Flux<EventAgg>

    @Query("SELECT s.* FROM event_agg s, event_qry q WHERE q.datasource = :datasource and q.id = s.qid order by s.edate, r.id")
    fun findAllByDatasource(datasource: String): Flux<EventAgg>
}
