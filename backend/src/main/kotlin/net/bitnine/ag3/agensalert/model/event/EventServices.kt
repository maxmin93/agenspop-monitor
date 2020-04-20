package net.bitnine.ag3.agensalert.model.event

import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull

import org.springframework.stereotype.Service

@Service
class EventQryService(private val repo: EventQryRepository) {

    suspend fun findAll() = repo.findAll().asFlow()
    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()
    suspend fun findByQid(qid: Long) = repo.findByQid(qid).awaitFirstOrNull()

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