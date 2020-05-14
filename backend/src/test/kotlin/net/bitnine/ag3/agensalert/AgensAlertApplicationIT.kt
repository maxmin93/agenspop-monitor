package net.bitnine.ag3.agensalert

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
class AgensAlertApplicationIT(@Autowired val client: WebTestClient) {

    @Test
    fun contextLoads() {
    }

    @Nested
    inner class Find {

        @Test
        fun `list of aggs`() {
        }

        @Test
        fun `existing agg returns OK`() {
        }

        @Test
        fun `inexisting agg returns NotFound`() {
        }

        @Test
        fun `id not a number returns BadRequest`() {
        }
    }

    @Nested
    inner class Search {

        @Test
        fun `returns OK`() {
        }

        @Test
        fun `empty email value returns BadRequest`() {
        }

        @Test
        fun `empty search returns BadRequest`() {
        }

        @Test
        fun `no search returns BadRequest`() {
        }
    }

    @Nested
    inner class Add {

        @Test
        fun `returns OK`() {
        }

        @Test
        fun `bad format returns BadRequest`() {
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `user exists returns OK`() {
        }

        @Test
        fun `id not a number returns BadRequest`() {
        }

        @Test
        fun `bad format returns BadRequest`() {
        }

        @Test
        fun `inexisting user returns NotFound`() {
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `returns Ok`() {
        }
    }
}