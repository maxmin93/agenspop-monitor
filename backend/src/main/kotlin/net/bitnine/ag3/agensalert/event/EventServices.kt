package net.bitnine.ag3.agensalert.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.bitnine.ag3.agensalert.gremlin.AgenspopClient
import net.bitnine.ag3.agensalert.gremlin.AgenspopUtil

import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


@Service
class EventQryService(
        private val repo: EventQryRepository,
        private val db: DatabaseClient
) {
    suspend fun findAll() = repo.findAll().asFlow()
    suspend fun findAllNotDeleted() = repo.findAllNotDeleted().asFlow()

    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()
    suspend fun findByQid(qid: Long) = repo.findByQid(qid).awaitFirstOrNull()
    suspend fun findByDatasource(datasource: String) = repo.findByDatasource(datasource).asFlow()

    suspend fun changeStateById(qid: Long, active_yn: Boolean) = repo.changeStateById(qid, active_yn).awaitFirstOrNull()
    suspend fun removeById(qid: Long) = repo.removeById(qid).awaitFirstOrNull()

    suspend fun addOne(qry: EventQry) = repo.save(qry).awaitFirstOrNull()
    suspend fun updateOne(id: Long, qry: EventQry): EventQry? {
        val existingQry = findById(id)
        return if (existingQry != null) repo.save(qry).awaitFirstOrNull() else null
    }
    suspend fun deleteOne(id: Long): Boolean {
        val existingQry = findById(id)
        return if (existingQry != null) {
            repo.delete(existingQry).awaitFirstOrNull()
            true
        } else false
    }

    // suspend fun findDateRangeByQid(qid: Long) = repo.findDataRangeByQid(qid).awaitFirstOrNull()
    suspend fun findDateRangeByQid(qid: Long): EventQryDateRange? {
        val dateRange = db.execute("SELECT q.id, q.name, q.datasource, q.script, a.from_date, a.to_date, a.cnt FROM EVENT_QRY q LEFT OUTER JOIN (SELECT qid, min(edate) as from_date, max(edate) as to_date, count(edate) as cnt FROM EVENT_AGG GROUP BY qid) a ON q.id = a.qid WHERE q.id = :qid")
                .bind("qid",qid)
                .fetch().awaitFirstOrNull()
        if( dateRange.isNullOrEmpty() ) return null
        else return EventQryDateRange(
                id = dateRange.get("ID") as Int,
                name = dateRange.get("NAME") as String,
                datasource = dateRange.get("DATASOURCE") as String,
                script = dateRange.get("SCRIPT") as String,
                from_date = dateRange.get("FROM_DATE") as LocalDate?,
                to_date = dateRange.get("TO_DATE") as LocalDate?,
                cnt = dateRange.get("CNT") as Long
        )
    }
}

// **NOTE :
// Non-blocking Spring Boot with Kotlin Coroutines
// https://www.baeldung.com/spring-boot-kotlin-coroutines


@Service
class EventRowService(
        private val repo: EventRowRepository,
        private val db: DatabaseClient,
        private val client: AgenspopClient
) {

    suspend fun findAll(): Flow<EventRow> {
        val stream = repo.findAll()
        return stream.asFlow()
    }
    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()
    suspend fun findByQid(qid: Long) = repo.findByQid(qid).awaitFirstOrNull()
    suspend fun findByQidAndDateRange(qid: Long, fromDate: LocalDate, toDate: LocalDate?): Flow<EventRow> {
        return repo.findAllByQidAndDateRange(qid, fromDate, if(toDate==null) LocalDate.now() else toDate).asFlow()
    }

    suspend fun addOne(row: EventRow) = repo.save(row).awaitFirstOrNull()
    suspend fun updateOne(id: Long, row: EventRow): EventRow? {
        val existingRow = findById(id)
        return if (existingRow != null) repo.save(row).awaitFirstOrNull() else null
    }
    suspend fun deleteOne(id: Long): Boolean {
        val existingRow = findById(id)
        return if (existingRow != null) {
            repo.delete(existingRow).awaitFirstOrNull()
            true
        } else false
    }

    suspend fun findEventsWithDateRange(qid: Long, fromDate: String, toDate: String?): Flow<Map<*, *>> {
        var from:LocalDate? = null
        var to:LocalDate? = null
        try {
            from = LocalDate.parse(fromDate, DateTimeFormatter.ISO_LOCAL_DATE);
            to = LocalDate.parse(toDate, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        catch (e: DateTimeParseException){ }
        if( from == null ) return emptyFlow()

        val rows = repo.findAllByQidAndDateRange(qid, from!!, to)
                .collectList().awaitFirstOrNull()
        if( rows.isNullOrEmpty() ) return emptyFlow()

        val idsSet = rows.flatMap{ it.ids!!.split(",") }.toSet()
        return client.findElementsWithDateRange(idsSet.toList(), fromDate, toDate).asFlow()
    }

    suspend fun findByQidWithTimeRange(qid: Long, fromDate: String, fromTime: String?): Flow<EventRow> {
        var dateValue: LocalDate? = null
        var timeValue: LocalTime? = null
        try {
            dateValue = LocalDate.parse(fromDate, DateTimeFormatter.ISO_LOCAL_DATE);
            timeValue = LocalTime.parse(if (fromTime.isNullOrBlank()) "00:00:00" else fromTime,
                    DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (e: DateTimeParseException) {
        }
        if (dateValue == null || timeValue == null) return emptyFlow()

        val fromDateTime = "${fromDate} ${if (fromTime.isNullOrBlank()) "00:00:00" else fromTime}"
        return repo.findAllByQidAndTimeRange(qid, fromDateTime).asFlow()
    }

    suspend fun findEventsWithTimeRange(qid: Long, fromDate: String, fromTime: String?): Flow<Map<*, *>> {
        var dateValue: LocalDate? = null
        var timeValue: LocalTime? = null
        try {
            dateValue = LocalDate.parse(fromDate, DateTimeFormatter.ISO_LOCAL_DATE);
            timeValue = LocalTime.parse( if(fromTime.isNullOrBlank()) "00:00:00" else fromTime,
                    DateTimeFormatter.ISO_LOCAL_TIME);
        }
        catch (e: DateTimeParseException){ }
        if( dateValue == null || timeValue == null ) return emptyFlow()

        val fromDateTime = "${fromDate} ${if(fromTime.isNullOrBlank()) "00:00:00" else fromTime}"
        val rows = repo.findAllByQidAndTimeRange(qid, fromDateTime)
                .collectList().awaitFirstOrNull()
        if( rows.isNullOrEmpty() ) return emptyFlow()

        val idsSet = rows.flatMap{ it.ids!!.split(",") }.toSet()
        return client.findElementsWithDateRange(idsSet.toList(), fromDateTime, null).asFlow()
    }

    // ** NOTE: modern 데이터와 rows 수집시 기록과 시간적 오차가 있다 ==> 동작은 되는거 확인함
    // http://localhost:8082/rows/search/time?qid=101&date=2019-03-23&time=02:15:17

}


@Service
class EventAggService(
        private val repo: EventAggRepository,
        private val db: DatabaseClient
){
    suspend fun findAll() = repo.findAll().asFlow()
    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()

    suspend fun findByQid(qid: Long) = repo.findByQid(qid).asFlow()
    suspend fun findByDateTerms(from: LocalDate, to: LocalDate?): Flow<EventAgg> {
        return repo.findAllByDateTerms(from, to).asFlow()
    }

    suspend fun addOne(agg: EventAgg) = repo.save(agg).awaitFirstOrNull()
    suspend fun updateOne(id: Long, agg: EventAgg): EventAgg? {
        val existingAgg = findById(id)
        return if (existingAgg != null) repo.save(agg).awaitFirstOrNull() else null
    }
    suspend fun deleteOne(id: Long): Boolean {
        val existingAgg = findById(id)
        return if (existingAgg != null) {
            repo.delete(existingAgg).awaitFirstOrNull()
            true
        } else false
    }

    suspend fun findDateRange(qid: Long): MutableMap<String, Any>? {
        // **NOTE: running queries
        // https://docs.spring.io/spring-data/r2dbc/docs/1.1.0.RELEASE/reference/html/#r2dbc.datbaseclient.queries
        // .asType<Map<String,Long>>()      // 안됨!! 생성자 호출함

        val dateRange = db.execute("SELECT min(s.edate) as from_date, max(s.edate) as to_date, count(edate) as cnt FROM event_agg s WHERE s.qid = :qid")
                .bind("qid", qid)
                .fetch().awaitFirstOrNull()

        // **NOTE: repository를 이용한 자유 쿼리는 안됨!!
        //     ==> EventAgg 생성자 호출하면서 오류남
        // val dateRange = repo.findDateRangeByQid(101).awaitFirstOrNull()

        return dateRange
    }

}

