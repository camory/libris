package fr.amory.libris.infra.web

import fr.amory.libris.domain.ReaderRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class MeControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val readers: ReaderRepository,
    private val jdbcClient: JdbcClient,
) {
    @BeforeEach
    fun emptyTheReaderTable() {
        jdbcClient.sql("truncate table reader").update()
    }

    @Test
    fun `a reader of the admin group is an admin`() {
        // Given
        val headers = listOf(
            "Remote-User" to "tophe",
            "Remote-Name" to "Tophe",
            "Remote-Email" to "tophe@amory.fr",
            "Remote-Groups" to "family,libris-admin",
        )

        // When
        val body = me(headers)

        // Then
        body shouldBe mapOf(
            "id" to readers.findByUsername("tophe")?.id.toString(),
            "username" to "tophe",
            "displayName" to "Tophe",
            "email" to "tophe@amory.fr",
            "role" to "ADMIN",
        )
    }

    @Test
    fun `a reader outside the admin group is a plain reader`() {
        // Given
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juliette",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        body shouldBe mapOf(
            "id" to readers.findByUsername("juliette")?.id.toString(),
            "username" to "juliette",
            "displayName" to "Juliette",
            "email" to "juliette@amory.fr",
            "role" to "READER",
        )
    }

    @Test
    fun `a reader without any group is a plain reader`() {
        // Given
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juliette",
            "Remote-Email" to "juliette@amory.fr",
        )

        // When
        val body = me(headers)

        // Then
        body shouldBe mapOf(
            "id" to readers.findByUsername("juliette")?.id.toString(),
            "username" to "juliette",
            "displayName" to "Juliette",
            "email" to "juliette@amory.fr",
            "role" to "READER",
        )
    }

    @Test
    fun `a reader without a display name is called by their username`() {
        // Given
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        body shouldBe mapOf(
            "id" to readers.findByUsername("juliette")?.id.toString(),
            "username" to "juliette",
            "displayName" to "juliette",
            "email" to "juliette@amory.fr",
            "role" to "READER",
        )
    }

    @Test
    fun `a first visit stores the reader`() {
        // Given
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juliette",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        val stored = readers.findByUsername("juliette")
        stored?.username shouldBe "juliette"
        stored?.email shouldBe "juliette@amory.fr"
        stored?.displayName shouldBe "Juliette"
        body?.get("id") shouldBe stored?.id.toString()
    }

    @Test
    fun `a later visit answers the stored reader whose display name Libris owns`() {
        // Given
        me(
            listOf(
                "Remote-User" to "juliette",
                "Remote-Name" to "Juliette",
                "Remote-Email" to "juliette@amory.fr",
                "Remote-Groups" to "family",
            ),
        )

        // When
        val body = me(
            listOf(
                "Remote-User" to "juliette",
                "Remote-Name" to "Juju",
                "Remote-Email" to "juju@amory.fr",
                "Remote-Groups" to "family",
            ),
        )

        // Then
        body shouldBe mapOf(
            "id" to readers.findByUsername("juliette")?.id.toString(),
            "username" to "juliette",
            "displayName" to "Juliette",
            "email" to "juliette@amory.fr",
            "role" to "READER",
        )
    }

    private fun me(headers: List<Pair<String, String>>): Map<String, String>? =
        client.get()
            .uri("/api/v1/me")
            .headers { headers.forEach { (name, value) -> it.add(name, value) } }
            .exchange()
            .expectStatus().isOk()
            .expectBody(object : ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult().responseBody
}
