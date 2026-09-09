package fr.amory.libris

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
    fun `a member of the admin group is an admin`() {
        val body = me(
            "Remote-User" to "tophe",
            "Remote-Name" to "Tophe",
            "Remote-Email" to "tophe@amory.fr",
            "Remote-Groups" to "family,libris-admin",
        )

        body shouldBe mapOf(
            "username" to "tophe",
            "displayName" to "Tophe",
            "email" to "tophe@amory.fr",
            "role" to "ADMIN",
        )
    }

    @Test
    fun `a member outside the admin group is a plain member`() {
        val body = me(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juliette",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        body shouldBe mapOf(
            "username" to "juliette",
            "displayName" to "Juliette",
            "email" to "juliette@amory.fr",
            "role" to "MEMBER",
        )
    }

    @Test
    fun `a member without any group is a plain member`() {
        val body = me(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juliette",
            "Remote-Email" to "juliette@amory.fr",
        )

        body shouldBe mapOf(
            "username" to "juliette",
            "displayName" to "Juliette",
            "email" to "juliette@amory.fr",
            "role" to "MEMBER",
        )
    }

    @Test
    fun `a member without a display name is called by their username`() {
        val body = me(
            "Remote-User" to "juliette",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        body shouldBe mapOf(
            "username" to "juliette",
            "displayName" to "juliette",
            "email" to "juliette@amory.fr",
            "role" to "MEMBER",
        )
    }

    private fun me(vararg headers: Pair<String, String>): Map<String, String>? =
        client.get()
            .uri("/api/v1/me")
            .headers { headers.forEach { (name, value) -> it.add(name, value) } }
            .exchange()
            .expectStatus().isOk()
            .expectBody(object : ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult().responseBody
}
