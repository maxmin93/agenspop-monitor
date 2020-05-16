package net.bitnine.ag3.agensalert.event

import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate


interface EventQryRepository : ReactiveCrudRepository<EventQry, Long> {

    // **NOTE: @Modifying
    // https://www.baeldung.com/spring-data-jpa-modifying-annotation
    // https://github.com/spring-projects/spring-data-r2dbc/blob/master/src/main/asciidoc/reference/r2dbc-repositories.adoc#modifying-queries

    @Modifying
    @Query("UPDATE event_qry q SET q.delete_yn = true, q.up_date = CURRENT_DATE() WHERE q.id = :qid")
    fun removeById(qid: Long): Mono<Integer?>

    @Modifying
    @Query("UPDATE event_qry q SET q.active_yn = :active_yn, q.up_date = CURRENT_DATE() WHERE q.id = :qid")
    fun changeStateById(qid: Long, active_yn: Boolean): Mono<Integer?>

    @Query("SELECT q.* FROM event_qry q WHERE q.id = :qid and q.delete_yn = false")
    fun findByQid(qid: Long): Mono<EventQry?>

    @Query("SELECT q.* FROM event_qry q WHERE q.delete_yn = false order by q.id")
    fun findAllNotDeleted(): Flux<EventQry>

    @Query("SELECT q.* FROM event_qry q WHERE q.datasource = :datasource and q.delete_yn = false")
    fun findByDatasource(datasource: String): Flux<EventQry>

    // **NOTE: not working ==> because repository must use TABLE mapping class
    @Query("SELECT q.id, q.name, q.datasource, q.script, a.from_date, a.to_date, a.cnt FROM EVENT_QRY q LEFT OUTER JOIN (SELECT qid, min(edate) as from_date, max(edate) as to_date, count(edate) as cnt FROM EVENT_AGG GROUP BY qid) a ON q.id = a.qid WHERE q.id = :qid")
    fun findDataRangeByQid(qid: Long): Mono<EventQryDateRange>
}

interface EventRowRepository : ReactiveCrudRepository<EventRow, Long> {

    @Query("SELECT r.* FROM event_row r WHERE r.qid = :qid order by id")
    fun findByQid(qid: Long): Flux<EventRow>

    @Query("SELECT r.* FROM event_row r WHERE r.qid = :qid and r.edate between :from and :to order by id")
    fun findAllByQidAndDateRange(qid: Long, from: LocalDate, to: LocalDate? = LocalDate.now()): Flux<EventRow>

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

    @Query("SELECT min(s.edate) as from_date, max(s.edate) as to_date, count(edate) as cnt FROM event_agg s WHERE s.qid = :qid")
    fun findDateRangeByQid(qid: Long): Mono<Map<String,Long>>

}
