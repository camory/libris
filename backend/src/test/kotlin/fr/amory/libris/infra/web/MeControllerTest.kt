package fr.amory.libris.infra.web

import fr.amory.libris.application.ReaderVisit
import fr.amory.libris.domain.Reader
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.UUID

private val TOPHE = Reader(
    id = UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f607181"),
    username = "tophe",
    email = "tophe@amory.fr",
    displayName = "Tophe",
)
private val JULIETTE = Reader(
    id = UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f607182"),
    username = "juliette",
    email = "juliette@amory.fr",
    displayName = "Juliette",
)

@SpringBootTest(classes = [WebSlice::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@MockitoBean(types = [ReaderVisit::class])
class MeControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val visit: ReaderVisit,
) {
    @Test
    fun `a reader of the admin group is an admin`() {
        // Given
        given(visit.visit("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)
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
            "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607181",
            "username" to "tophe",
            "displayName" to "Tophe",
            "email" to "tophe@amory.fr",
            "role" to "ADMIN",
        )
    }

    @Test
    fun `a reader outside the admin group is a plain reader`() {
        // Given
        given(visit.visit("juliette", "juliette@amory.fr", "Juliette")).willReturn(JULIETTE)
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
            "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607182",
            "username" to "juliette",
            "displayName" to "Juliette",
            "email" to "juliette@amory.fr",
            "role" to "READER",
        )
    }

    @Test
    fun `a reader without any group is a plain reader`() {
        // Given
        given(visit.visit("juliette", "juliette@amory.fr", "Juliette")).willReturn(JULIETTE)
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juliette",
            "Remote-Email" to "juliette@amory.fr",
        )

        // When
        val body = me(headers)

        // Then
        body?.get("role") shouldBe "READER"
    }

    @Test
    fun `a reader without a display name visits under their username`() {
        // Given
        given(visit.visit("juliette", "juliette@amory.fr", "juliette"))
            .willReturn(JULIETTE.copy(displayName = "juliette"))
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        body?.get("displayName") shouldBe "juliette"
    }

    @Test
    fun `the answer is the reader of the visit, not the one of the headers`() {
        // Given
        given(visit.visit("juliette", "juju@amory.fr", "Juju")).willReturn(JULIETTE)
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juju",
            "Remote-Email" to "juju@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        body shouldBe mapOf(
            "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607182",
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
