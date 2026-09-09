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
        val body = client.get()
            .uri("/api/v1/me")
            .header("Remote-User", "tophe")
            .header("Remote-Name", "Tophe")
            .header("Remote-Email", "tophe@amory.fr")
            .header("Remote-Groups", "family,libris-admin")
            .exchange()
            .expectStatus().isOk()
            .expectBody(object : ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult().responseBody

        body shouldBe mapOf(
            "username" to "tophe",
            "displayName" to "Tophe",
            "email" to "tophe@amory.fr",
            "role" to "ADMIN",
        )
    }
}
