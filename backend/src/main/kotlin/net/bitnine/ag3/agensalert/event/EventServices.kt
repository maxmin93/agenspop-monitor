package net.bitnine.ag3.agensalert.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull

import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class EventQryService(private val repo: EventQryRepository) {

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
}

// **NOTE :
// Non-blocking Spring Boot with Kotlin Coroutines
// https://www.baeldung.com/spring-boot-kotlin-coroutines


@Service
class EventRowService(
        private val repo: EventRowRepository,
        private val db: DatabaseClient
) {

    suspend fun findAll(): Flow<EventRow> {
        val stream = repo.findAll()
        return stream.asFlow()
    }
    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()

    suspend fun findByQid(qid: Long) = repo.findByQid(qid).awaitFirstOrNull()
    suspend fun findByDateTerms(from: LocalDate, to: LocalDate?): Flow<EventRow> {
        return repo.findAllByDateTerms(from, to).asFlow()
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
                .bind("qid",101L)
                .fetch().awaitFirstOrNull()

        // **NOTE: repository를 이용한 자유 쿼리는 안됨!!
        //     ==> EventAgg 생성자 호출하면서 오류남
        // val dateRange = repo.findDateRangeByQid(101).awaitFirstOrNull()

        // println( "** dateRange: ${dateRange}")
        // ==> dateRange: {FROM_DATE=2019-01-21, TO_DATE=2019-03-23, CNT=2}

        return dateRange
    }

}

