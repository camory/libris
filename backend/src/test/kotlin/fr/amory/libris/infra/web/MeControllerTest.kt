package fr.amory.libris.infra.web

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class MeControllerTest @Autowired constructor(
    private val client: RestTestClient,
) {
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
        body?.minus("id") shouldBe mapOf(
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
        body?.minus("id") shouldBe mapOf(
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
        body?.minus("id") shouldBe mapOf(
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
        body?.minus("id") shouldBe mapOf(
            "username" to "juliette",
            "displayName" to "juliette",
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
