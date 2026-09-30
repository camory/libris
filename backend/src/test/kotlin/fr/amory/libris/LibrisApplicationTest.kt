package fr.amory.libris

import fr.amory.libris.bibliography.infrastructure.lookup.SourcesProperties
import fr.amory.libris.bibliography.infrastructure.persistence.CoversProperties
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.client.RestTestClient
import java.nio.file.Path
import java.time.Duration.ofSeconds

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["LIBRIS_COVERS_DIR=build/test-covers"])
@AutoConfigureRestTestClient
class LibrisApplicationTest @Autowired constructor(
    private val client: RestTestClient,
    private val sources: SourcesProperties,
    private val covers: CoversProperties,
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
    fun `the covers live in the directory it is given`() {
        covers shouldBe CoversProperties(dir = Path.of("build/test-covers"))
    }
}
