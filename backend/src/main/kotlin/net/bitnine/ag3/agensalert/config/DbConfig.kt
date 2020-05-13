package net.bitnine.ag3.agensalert.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.r2dbc.spi.ConnectionFactory
import net.bitnine.ag3.agensalert.gremlin.AgenspopClient
import net.bitnine.ag3.agensalert.model.employee.Employee
import net.bitnine.ag3.agensalert.model.employee.EmployeeRepository
import net.bitnine.ag3.agensalert.gremlin.AgenspopUtil
import net.bitnine.ag3.agensalert.model.event.*
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream
import kotlin.random.Random


@Component
class DbConfig(
    // @Autowired val connectionFactory: ConnectionFactory
) {

    @Bean
    fun initEmpoyee(employeeRepository: EmployeeRepository, db: DatabaseClient) = ApplicationRunner {

        val initDb = db.execute {
            """
                DROP TABLE if exists employee;
                CREATE TABLE employee (
                    id SERIAL PRIMARY KEY,
                    first_name VARCHAR(255) NOT NULL,
                    last_name VARCHAR(255) NOT NULL
                );
            """
        }

        val stream = Stream.of(
                Employee(null, "Petros", "S"),
                Employee(null, "Christos", "M")
        )
        val saveAll = employeeRepository.saveAll(Flux.fromStream(stream))

        initDb
                .then()
                .thenMany(saveAll)
                .subscribe{ println("init: $it")}
    }

/*
    @Bean
    fun initEventRow(
            aggRepository: EventAggRepository,
            rowRepository: EventRowRepository,
            qryRepository: EventQryRepository,
            apiClient: AgenspopClient,
            db: DatabaseClient
    ) = ApplicationRunner {
        // **ref. https://www.baeldung.com/spring-data-r2dbc

        val sdate: LocalDate = LocalDate.of(2018,5,10)
        val edate: LocalDate = LocalDate.now()  // LocalDate.of(2019,5,10)
        var rows = arrayListOf<EventRow>()

        val queries = qryRepository.findAllNotDeleted().collectList().block();
        for( dt in sdate..edate ){
            val fromDate = DateTimeFormatter.ofPattern("yyyyMMdd").format(dt)
            val toDate = DateTimeFormatter.ofPattern("yyyyMMdd").format(dt.plusDays(1))
             println("\nfrom '$fromDate' ~ to '$toDate' : ")

            for( query in queries!!){
                val results = apiClient.execGremlin(query.datasource, query.script, fromDate, toDate)
                        .filter{ e-> !e.isNullOrEmpty() && e.containsKey("group") && e.containsKey("data") && e.containsKey("scratch") }
                        .map{
                            e-> mapOf<String,String>(
                                "group" to e.get("group").toString(),
                                "id" to (e.get("data") as Map<String,Any>).get("id").toString(),
                                "label" to (e.get("data") as Map<String,Any>).get("label").toString(),
                                "created" to (e.get("scratch") as Map<String,Any>).get("_\$\$created").toString()
                        )
                        }
                        .filter{ e-> !e.isEmpty() }
                        .collectList().block()

                // ** QUESTION
                //    없는 데이터를 넣을 필요가 있을까? 단순 모니터링인데..

                if( results.isNullOrEmpty().not() ) {
                    println("    ${query.id}: ${query.datasource}_${query.script} ==> ${results!!.size}")
                    val row = AgenspopUtil.makeRowFromResults(dt, query, results!!)
                    rows.add(row)
                }
//                else{
//                    val emptyRow = EventRow(id = null, qid=query.id!!, type=null,
//                            ids_cnt=0L, ids="", labels="",
//                            edate= dt, etime= LocalTime.now())
//                    rows.add(emptyRow)
//                }
            }

        }
        println("\n\ninsert rows="+rows.size+"\n")

        val initAgg = db.execute(
"""truncate table event_agg;
-- delete from event_agg where edate >= DATE '2018-01-01'
;
merge into event_agg(id, edate, qid, type, labels, row_cnt, ids_cnt)
select TRANSACTION_ID(), edate, qid, listagg(type,','), listagg(labels,','), count(id), sum(ids_cnt)
from event_row
where type is not null and edate >= DATE '2018-05-10'
group by edate, qid
order by edate, qid
;"""
        )

        val saveAll = rowRepository.saveAll(Flux.fromStream(rows.stream()))

        saveAll.then(initAgg.then()).subscribe({
            println("** Success: $it")
        },{
            println("** Error: $it")
        },{
            println("** Completed: $it")
        })
    }
*/
    fun randomIds(size: Long): String {
        var ids = arrayListOf<String>()
        var count = size
        while( count > 0 ){
            ids.add("modern_"+Random.nextInt(1, 100))
            count -= 1
        }
        return ids.joinToString(separator=",")      //writer.writeValueAsString(ids)
    }

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

    @Bean
    fun initConnTest(connectionFactory: ConnectionFactory) = ApplicationRunner {

        // **NOTE :
        // https://www.baeldung.com/r2dbc

        Mono.from(connectionFactory.create())
                .flatMap{ c ->
                    Mono.from(c
                        .createStatement("select * from event_row where id = $1")
                        .bind("$1", 101)
                        .execute()
                    )
                    .doFinally{ st ->       // st: SignalType
                        c?.close()
                        println("** close: initEventsList")
                    }
                }

    }
}