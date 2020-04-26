package net.bitnine.ag3.agensalert.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.bitnine.ag3.agensalert.model.employee.EmployeeRepository
import net.bitnine.ag3.agensalert.model.employee.Employee
import net.bitnine.ag3.agensalert.model.event.EleType
import net.bitnine.ag3.agensalert.model.event.EventQryRepository
import net.bitnine.ag3.agensalert.model.event.EventRow
import net.bitnine.ag3.agensalert.model.event.EventRowRepository

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.ranges.rangeTo as rangeTo

@Component
class DbConfig {

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
                .subscribe{ println("init: Employee $it")}
    }

    @Bean
    fun initEventRow(
            rowRepository: EventRowRepository,
            qryRepository: EventQryRepository,
            db: DatabaseClient
    ) = ApplicationRunner {
        // **ref. https://www.baeldung.com/spring-data-r2dbc

        val sdate: LocalDate = LocalDate.of(2018,1,1)
        val edate: LocalDate = LocalDate.now()
        var rows = arrayListOf<EventRow>()

        val mapper = jacksonObjectMapper()
        val writer = mapper.writerFor(object: TypeReference<List<String>>(){})


//        @Id val id: Long? = null,
//        @Column("qid") val qid: Long,
//        @Column("type") val type: String,
//        @Column("labels") val labels: Array<String>,
//        @Column("ids") val ids: Array<String>,
//        @Column("edate") val edate: Date,
//        @Column("etime") val etime: Timestamp

        for( dt in sdate..edate ){
            val qid = Random.nextLong(101,109)
            val etime = LocalTime.of(Random.nextInt(0, 24), Random.nextInt(0, 60))
            // val labels : String = writer.writeValueAsString(listOf("person", "software"))
            val row = EventRow(id = null, qid=qid, type="nodes", ids=randomIds(writer),
                    labels=listOf("person", "software").joinToString(separator=","),
                    edate=dt, etime=etime)
            rows.add(row)
        }
        println("insert rows="+rows.size+"\n")

// **NOTE :
// Binding Values to Queries
// https://github.com/spring-projects/spring-data-r2dbc/blob/master/src/main/asciidoc/reference/r2dbc-sql.adoc
// ==> DATE 에 대한 bind 가 안됨 (통채로 문자열화 시켜 넣어야 작동)

//      -- truncate table event_agg;
        val initAgg = db.execute(
            """
merge into event_agg(id, edate, qid, type, labels, row_cnt)
select TRANSACTION_ID(), edate, qid, type, listagg(labels,','), count(id)
from event_row
where edate >= DATE '2019-01-01'
group by edate, qid, type
order by edate, qid, type;
            """
        )
        //.bind("from", LocalDate.of(2019,1,1))  //"'2019-01-01'")    // LocalDate.of(2019,1,1))

        val saveAll = rowRepository.saveAll(Flux.fromStream(rows.stream()))
        saveAll.then(initAgg.then()).subscribe({},{
            println("** Error: $it")
        },{
            println("** Completed: $it")
        })
    }

    fun randomIds(writer: ObjectWriter): String {
        var count = Random.nextInt(1, 10)
        var ids = arrayListOf<String>()
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
}