package net.bitnine.ag3.agensalert.model.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.core.DatabaseClient

import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toFlux

@Service
class EventQryService(private val repo: EventQryRepository) {

    suspend fun findAll() = repo.findAll().asFlow()
    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()
    suspend fun findByQid(qid: Long) = repo.findByQid(qid).awaitFirstOrNull()
    suspend fun findByDatasource(datasource: String) = repo.findByDatasource(datasource).asFlow()

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

@Service
class EventRowService(
        private val repo: EventRowRepository,
        private val db: DatabaseClient
) {

    suspend fun findAll() = repo.findAll().asFlow()
    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()
    suspend fun findByQid(qid: Long) = repo.findByQid(qid).awaitFirstOrNull()

    suspend fun findArray() {
        val rs = db
                .execute("select id, qid, type, labels from event_row;").fetch().all()

        rs.toFlux()
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
class EventStatService(private val repo: EventStatRepository) {

    suspend fun findAll() = repo.findAll().asFlow()
    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()
    suspend fun findByQid(qid: Long) = repo.findByQid(qid).awaitFirstOrNull()

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

}