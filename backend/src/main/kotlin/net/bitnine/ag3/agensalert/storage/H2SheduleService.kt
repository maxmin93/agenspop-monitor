package net.bitnine.ag3.agensalert.storage

import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import net.bitnine.ag3.agensalert.config.MonitorProperties
import net.bitnine.ag3.agensalert.event.EventAggRepository
import net.bitnine.ag3.agensalert.event.EventQryRepository
import net.bitnine.ag3.agensalert.event.EventRow
import net.bitnine.ag3.agensalert.event.EventRowRepository
import net.bitnine.ag3.agensalert.gremlin.AgenspopClient
import net.bitnine.ag3.agensalert.gremlin.AgenspopUtil

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitFirst
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct
import javax.swing.text.DateFormatter

// **참고: Kotlin으로 서버 백엔드 개발
// https://engineering.linecorp.com/ko/blog/server-side-kotlin-clova-skill-challenge/

@Service
class H2SheduleService(
        @Autowired val monitorProperties: MonitorProperties,
        @Autowired val qryRepository: EventQryRepository,
        @Autowired val rowRepository: EventRowRepository,
        @Autowired val db: DatabaseClient,
        @Autowired val client: AgenspopClient
){

    private val logger = LoggerFactory.getLogger(H2SheduleService::class.java)
    private val dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private var cronInterval:Long = 30

    @Volatile private var canScheduled: Boolean = false

    // **NOTE: by lazy를 통한 초기화 지연
    // https://medium.com/til-kotlin-ko/kotlin-delegated-property-by-lazy%EB%8A%94-%EC%96%B4%EB%96%BB%EA%B2%8C-%EB%8F%99%EC%9E%91%ED%95%98%EB%8A%94%EA%B0%80-74912d3e9c56

    @PostConstruct
    private fun init(){
        cronInterval = monitorProperties.cronInterval()

        // do activate after check storage
        checkStorage()
    }

    fun setActivate(state:Boolean){ canScheduled = state }
    fun isActivate():Boolean = canScheduled

    @Scheduled(cron = "\${agens.monitor.cron-daily}")       //  매일 1회 (cron 에서는 initialDelay 안됨)
    fun taskDaily() {
        val currDateTime = LocalDateTime.now()
        val truncatedDateTime = currDateTime.plusSeconds(2).truncatedTo(ChronoUnit.MINUTES)
        val fromDate = truncatedDateTime.minusDays(1).toLocalDate()
        logger.info("daily task => ${DateTimeFormatter.ISO_DATE.format(fromDate)}")

        // grouping EvnetRows by qid, edate and insert to EventAgg
        if( canScheduled ) batchDaily(fromDate)
    }

    @Scheduled(cron = "\${agens.monitor.cron-realtime}")    //  기본: 30초마다
    fun taskRealtime() {
        val currDateTime = LocalDateTime.now()
        val truncatedDateTime = currDateTime.plusNanos(300).truncatedTo(ChronoUnit.SECONDS)
        val fromTime = truncatedDateTime.minusSeconds(cronInterval)
        // logger.info("every 30 seconds : from ${dtFormatter.format(minus30sec)} ~ to ${dtFormatter.format(truncatedDateTime)}")

        // parsing query responses and insert to EventRow
        if( canScheduled ) batchRealtime(fromTime)
    }

    ////////////////////////////////////////////////////////

    // **NOTE: coroutine 에 의해 실행되므로 suspend 붙으면 안됨
    fun batchRealtime(fromTime:LocalDateTime):Unit {
        println("\nretrieval every ${cronInterval} seconds... from '${dtFormatter.format(fromTime)}'")
        // for DEBUG
        // val fromTime = LocalDateTime.of(2019, 8, 22, 10, 10, 10)

        qryRepository.findAllNotDeleted().subscribe {
            val query = it
            client.execGremlinWithRange(it.datasource, it.script, dtFormatter.format(fromTime), null)
                .filter{ e-> !e.isNullOrEmpty() && e.containsKey("group") && e.containsKey("data") && e.containsKey("scratch") }
                .map{ e-> mapOf<String,String>(
                        "group" to e.get("group").toString(),
                        "id" to (e.get("data") as Map<String,Any>).get("id").toString(),
                        "label" to (e.get("data") as Map<String,Any>).get("label").toString(),
                        "timestamp" to (e.get("scratch") as Map<String,Any>).get("_\$\$timestamp").toString()
                )}
                .collectList()
                .subscribe {
                    if( it.isEmpty() ) return@subscribe;

                    val row = AgenspopUtil.makeRowFromResults(fromTime, query, it!!)
                    rowRepository.save(row).subscribe({
                        print("- qid=${row.qid}: <${query.datasource}>, types=[${row.type}], labels=[${row.labels}], ids_cnt=${row.ids_cnt}")
                        println(" ==> OK, id=${it.id}")
                    },
                    {
                        print("- qid=${row.qid}: <${query.datasource}>, types=[${row.type}], labels=[${row.labels}], ids_cnt=${row.ids_cnt}")
                        println(" ==> fail, err=${it}")
                    })
                }
        }
    }

    // **NOTE: coroutine 에 의해 실행되므로 suspend 붙으면 안됨
    fun batchDaily(fromDate:LocalDate):Unit {
        print("\nbatch daily [ ${monitorProperties.cronDaily} ] ... from '${fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}'")

        db.execute("""delete from event_agg where edate >= :fromDate;
merge into event_agg(id, edate, qid, type, labels, row_cnt, ids_cnt)
select TRANSACTION_ID(), edate, qid, listagg(type,','), listagg(labels,','), count(id), sum(ids_cnt)
from event_row
where type is not null and edate >= :fromDate
group by edate, qid
order by edate, qid;""")
                .bind("fromDate", fromDate)
                .fetch().rowsUpdated()
                .subscribe({
                    println(" ==> OK, rows=${it}")
                }
                , {
                    println(" ==> fail, err=${it}")
                })
    }

    ////////////////////////////////////////////////////////

    suspend fun batchAll(fromDate: LocalDate){
        println("\nbefore batch task, stop scheduledTasks and wait a moment..")
        this.canScheduled = false

        Thread.sleep(1500)

        println("\ntruncate event_row and event_agg table... ")
        db.execute("truncate table event_agg; truncate table event_row;")
                .then().awaitFirstOrNull()

        println("\nget queries... ")
        val queries = qryRepository.findAllNotDeleted()
                .collectList().awaitLast()

        Thread.sleep(500)

        println("\nEach query:")
        val fromTime:LocalDateTime = fromDate.atStartOfDay()
        for( query in queries!! ){

            println("- get query[${query.id}]... ${query.script}")
            val results = client.execGremlinWithRange(query.datasource, query.script, dtFormatter.format(fromTime), null)
                .filter { e -> !e.isNullOrEmpty() && e.containsKey("group") && e.containsKey("data") && e.containsKey("scratch") }
                .map { e ->
                    mapOf<String, String>(
                            "group" to e.get("group").toString(),
                            "id" to (e.get("data") as Map<String, Any>).get("id").toString(),
                            "label" to (e.get("data") as Map<String, Any>).get("label").toString(),
                            "timestamp" to (e.get("scratch") as Map<String, Any>).get("_\$\$timestamp").toString()
                    )
                }
                .collectList()
                .awaitLast()

            if (results.isNullOrEmpty()) continue;

            results.sortBy { it.get("timestamp") }
            val grpByEdate = results.map {
                    val evt = it.toMutableMap()
                    val edate = it.get("timestamp")!!.split(" ")[0]
                    evt.set("edate", edate)
                    return@map evt
                }
                .groupBy {
                    it.get("edate")
                }

            val keys = grpByEdate.keys.sortedBy { it }
            for (edate in keys) {
                val row = AgenspopUtil.makeRowFromResults(fromTime, query, grpByEdate.get(edate) as List<Map<String, Any>>)
                rowRepository.save(row).subscribe({
                        print("  + qid=${row.qid}: <${query.datasource}>, types=[${row.type}], labels=[${row.labels}], ids_cnt=${row.ids_cnt}")
                        println(" ==> OK, id=${it.id}")
                    },
                    {
                        print("  + qid=${row.qid}: <${query.datasource}>, types=[${row.type}], labels=[${row.labels}], ids_cnt=${row.ids_cnt}")
                        println(" ==> fail, err=${it}")
                    })
            }
        }

        Thread.sleep(1500)

        println("\ngroupBy and insert Aggregation..")
        val updated_cnt = db.execute("""merge into event_agg(id, edate, qid, type, labels, row_cnt, ids_cnt)
select TRANSACTION_ID(), edate, qid, listagg(type,','), listagg(labels,','), count(id), sum(ids_cnt)
from event_row
where type is not null and edate >= :fromDate
group by edate, qid
order by edate, qid
;""")
                .bind("fromDate", fromDate)
                .fetch().rowsUpdated().awaitFirstOrNull()

        println("\bfinish batch task: agg_size=${updated_cnt}")

        Thread.sleep(500)

        println("\nthen, start scheduledTasks.\n")
        this.canScheduled = true

/*
        qryRepository.findAllNotDeleted().subscribe({
            val query = it
            client.execGremlin(it.datasource, it.script, dtFormatter.format(fromTime), null)
                .filter { e -> !e.isNullOrEmpty() && e.containsKey("group") && e.containsKey("data") && e.containsKey("scratch") }
                .map { e ->
                    mapOf<String, String>(
                            "group" to e.get("group").toString(),
                            "id" to (e.get("data") as Map<String, Any>).get("id").toString(),
                            "label" to (e.get("data") as Map<String, Any>).get("label").toString(),
                            "timestamp" to (e.get("scratch") as Map<String, Any>).get("_\$\$timestamp").toString(),
                    )
                }
                .collectList()
                .subscribe {
                    if (it.isEmpty()) return@subscribe;

                    it.sortBy { it.get("timestamp") }
                    val grpByEdate = it.map {
                        val evt = it.toMutableMap()
                        val edate = it.get("timestamp")!!.split(" ")[0]
                        evt.set("edate", edate)
                        return@map evt
                    }
                            .groupBy { it.get("edate") }
                    val keys = grpByEdate.keys.sortedBy { it }

                    for (edate in keys) {
                        val row = AgenspopUtil.makeRowFromResults(fromTime, query, grpByEdate.get(edate) as List<Map<String, Any>>)
                        rowRepository.save(row).subscribe({
                            print("- qid=${row.qid}: <${query.datasource}>, types=[${row.type}], labels=[${row.labels}], ids_cnt=${row.ids_cnt}")
                            println(" ==> OK, id=${it.id}")
                        },
                        {
                            print("- qid=${row.qid}: <${query.datasource}>, types=[${row.type}], labels=[${row.labels}], ids_cnt=${row.ids_cnt}")
                            println(" ==> fail, err=${it}")
                        })
                    }
                }
        },{
            println("onError")
        },{
            println("Completed!")
        })
 */

    }

    ////////////////////////////////////////////////////////

    fun checkStorage():Unit {
        var isChecked = false

        println("\ncheck agenspop client and embedded storage before scheduleTasks start..")
        while( isChecked.not() ){
            Thread.sleep(1500L)     // initial delay
            if( isChecked ) break

            client.findDatasources().subscribe({
                println("  1) check agenspop client.. OK (${it.keys})")

                db.execute("""select count(*) as cnt from event_row""")
                        .fetch().first().subscribe({
                            println("  2) check event_row table.. OK (size=${it.get(it.keys.first())})\n")

                            // activate schedule tasks
                            this.canScheduled = true
                            isChecked = true
                        },{
                            println("  2) check event_ro table.. fail (msg=${it})\n")
                        })
            },{
                println("  1) check agenspop client.. fail (msg=${it})\n")
            })
        }
    }

    ////////////////////////////////////////////////////////

    operator fun ClosedRange<LocalDate>.iterator() : Iterator<LocalDate> {
        return object : Iterator<LocalDate> {
            private var next = this@iterator.start
            private val finalElement = this@iterator.endInclusive
            private var hasNext = !next.isAfter(this@iterator.endInclusive)
            override fun hasNext(): Boolean = hasNext

            override fun next(): LocalDate {
                val value = next
                if (value == finalElement) {
                    hasNext = false
                } else {
                    next = next.plusDays(1)
                }
                return value
            }
        }
    }
}