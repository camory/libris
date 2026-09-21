package fr.amory.libris

import fr.amory.libris.bibliography.infrastructure.lookup.SourcesProperties
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.client.RestTestClient
import java.time.Duration.ofSeconds

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class LibrisApplicationTest @Autowired constructor(
    private val client: RestTestClient,
    private val sources: SourcesProperties,
) {
    @Test
    fun `the application starts and reports itself healthy`() {
        client.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.status").isEqualTo("UP")
    }

    @Test
    fun `the sources are configured with their defaults`() {
        sources shouldBe SourcesProperties(
            bnfUrl = "https://catalogue.bnf.fr/api/SRU",
            openLibraryUrl = "https://openlibrary.org",
            timeout = ofSeconds(5),
        )
    }
}
