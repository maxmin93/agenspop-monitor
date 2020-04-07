package net.bitnine.ag3.agensalert

import net.bitnine.ag3.agensalert.employee.EmployeeRepository
import net.bitnine.ag3.agensalert.employee.Employee

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.stream.Stream

@Component
class DbConfiguration {

    @Bean
    fun runner(employeeRepository: EmployeeRepository, db: DatabaseClient) = ApplicationRunner {

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
}