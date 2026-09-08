package fr.amory.libris

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient
import tools.jackson.databind.ObjectMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LibrisApplicationTest @Autowired constructor(
    private val client: RestTestClient,
    private val objectMapper: ObjectMapper,
) {
    @Test
    fun `actuator info reports the version the build was given`() {
        val body = client.get().uri("/actuator/info").exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody

        objectMapper.readTree(body).path("build").path("version").asString() shouldBe
            System.getProperty("libris.version")
    }
}
