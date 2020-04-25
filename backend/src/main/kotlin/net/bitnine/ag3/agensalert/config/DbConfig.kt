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
                .subscribe()
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
        var rowStream = arrayListOf<EventRow>()

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
            val labels : String = writer.writeValueAsString(listOf("person", "software"))
            val row = EventRow(id = null, qid=qid, type="nodes", labels=labels,
                    edate=dt, etime=etime)
            rowStream.add(row)
        }
        // rowStream.forEach{ println(it) }


        val stream = Stream.of(
            EventRow(qid=101, type="nodes", labels=writer.writeValueAsString(listOf("person", "software"))),
            EventRow(qid=107, type="nodes")
//            EventRow(id=null, qid=101, type="nodes", labels=arrayOf("person","software"), ids=arrayOf("modern_73","modern_24","modern_70","modern_99") ),
//                    // , edate=LocalDate.of(2020,4,23), etime=LocalTime.of(2,45)),
//            EventRow(id=null, qid=107, type="nodes", labels=arrayOf("person","software"), ids=arrayOf("modern_70","modern_84","modern_34","modern_20") )
//                    // , edate=LocalDate.of(2020,4,24), etime=LocalTime.of(10,22))
        )
        val saveAll = rowRepository.saveAll(Flux.fromStream(stream))
        saveAll.subscribe()
    }

    fun randomIds(writer: ObjectWriter): String {
        var count = Random.nextInt(1, 10)
        var ids = arrayListOf<String>()
        while( count > 0 ){
            ids.add("modern_"+Random.nextInt(1, 100))
            count -= 1
        }
        return writer.writeValueAsString(ids)
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