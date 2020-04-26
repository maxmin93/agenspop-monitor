package net.bitnine.ag3.agensalert.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.Converter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

import java.util.*

@Configuration
@EnableR2dbcRepositories
class ReactiveH2Config : AbstractR2dbcConfiguration() {

    val objectMapper: ObjectMapper = jacksonObjectMapper();

    @Bean
    override fun connectionFactory(): ConnectionFactory {
//        return ConnectionFactories
//                .get("r2dbc:h2:mem:///agens?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
        return H2ConnectionFactory(
                H2ConnectionConfiguration.builder()
                        .inMemory("agens")      // .file(<path>)
                        .option("DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                        .username("sa")
                        .password("")
                        .build()
        )
    }

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        val populator = ResourceDatabasePopulator(ClassPathResource("schema.sql"))
        initializer.setDatabasePopulator(populator)
        return initializer
    }

    @Bean
    override fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters: MutableList<Converter<*,*>> = ArrayList<Converter<*,*>>()
        converters.add(ArrayToListConverter(objectMapper) as Converter<*,*>)
        converters.add(ListToArrayConverter(objectMapper) as Converter<*,*>)
        return R2dbcCustomConversions(storeConversions, converters)
    }

}