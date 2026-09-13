package fr.amory.libris

import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source.BNF
import fr.amory.libris.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.infra.lookup.SourcesProperties
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
    private val isbnSources: List<IsbnSource>,
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

    @Test
    fun `the BnF is asked before Open Library`() {
        isbnSources.map { it.source } shouldBe listOf(BNF, OPEN_LIBRARY)
    }
}
