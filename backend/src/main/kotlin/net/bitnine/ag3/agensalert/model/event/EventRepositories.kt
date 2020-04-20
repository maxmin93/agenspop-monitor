package net.bitnine.ag3.agensalert.model.event

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface EventQryRepository : ReactiveCrudRepository<EventQry, Long> {

    @Query("SELECT q.* FROM event_qry q WHERE q.id = :qid and q.delete_yn = false")
    fun findByQid(qid: Long): Mono<EventQry?>

    @Query("SELECT q.* FROM event_qry q WHERE q.delete_yn = false")
    fun findAllNotDeleted(): Flux<EventQry>

    @Query("SELECT q.* FROM event_qry q WHERE q.datasource = :datasource and q.delete_yn = false")
    fun findByDatasource(datasource: String): Flux<EventQry>
}

interface EventRowRepository : ReactiveCrudRepository<EventRow, Long> {

    @Query("SELECT r.* FROM event_row r WHERE r.qid = :qid order by id")
    fun findByQid(qid: Long): Flux<EventRow>

    @Query("SELECT r.* FROM event_row r WHERE r.edate between :sdate and :edate")
    fun findAllByDateTerms(sdate: Date, edate: Date): Flux<EventRow>

    @Query("SELECT r.* FROM event_row r, event_qry q WHERE q.datasource = :datasource and q.id = r.qid order by r.edate, r.id")
    fun findAllByDatasource(datasource: String): Flux<EventRow>
}

interface EventStatRepository : ReactiveCrudRepository<EventStat, Long> {

    @Query("SELECT s.* FROM event_stat s WHERE s.qid = :qid order by id")
    fun findByQid(qid: Long): Flux<EventStat>

    @Query("SELECT s.* FROM event_stat s WHERE s.edate between :sdate and :edate")
    fun findAllByDateTerms(sdate: Date, edate: Date): Flux<EventStat>

    @Query("SELECT s.* FROM event_stat s, event_qry q WHERE q.datasource = :datasource and q.id = s.qid order by s.edate, r.id")
    fun findAllByDatasource(datasource: String): Flux<EventStat>
}
