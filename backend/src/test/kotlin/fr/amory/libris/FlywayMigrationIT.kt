package fr.amory.libris

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class FlywayMigrationIT(private val jdbc: JdbcClient) {

    @Test
    fun `V001 created the unaccent and pg_trgm extensions`() {
        val extensions = jdbc.sql("SELECT extname FROM pg_extension").query(String::class.java).list()

        extensions shouldContainAll listOf("unaccent", "pg_trgm")
    }

    @Test
    fun `V001 is recorded as a successful migration`() {
        val success = jdbc
            .sql("SELECT success FROM flyway_schema_history WHERE script = 'V001__extensions.sql'")
            .query(Boolean::class.java)
            .list()

        success shouldBe listOf(true)
    }
}
