package net.bitnine.ag3.agensalert.service

import kotlinx.coroutines.reactive.asFlow
import net.bitnine.ag3.agensalert.gremlin.AgenspopClient
import net.bitnine.ag3.agensalert.gremlin.AgenspopService
import net.bitnine.ag3.agensalert.model.event.EventAggRepository
import net.bitnine.ag3.agensalert.model.event.EventAggService
import net.bitnine.ag3.agensalert.model.event.EventRowRepository
import net.bitnine.ag3.agensalert.model.event.EventRowService
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class MonitorServices(
    private val aggRepository: EventAggRepository,
    private val rowRepository: EventRowRepository,
    private val client: AgenspopClient
){

    @PostConstruct
    private fun init(){
        // too early call before created Table
    }

    suspend fun getQryTargets(qid: Long){
        aggRepository.findByQid(qid).collectList().subscribe {
            if(it.isEmpty()) return@subscribe

            println("agg results of ${qid} = ${it.size}")
        }
    }
}