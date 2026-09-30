package fr.amory.libris

import fr.amory.libris.bibliography.infrastructure.lookup.SourcesProperties
import fr.amory.libris.bibliography.infrastructure.persistence.CoversConfig
import fr.amory.libris.bibliography.infrastructure.persistence.CoversProperties
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.StandardEnvironment
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

    @Test
    fun `the covers need the directory they are given`() {
        ApplicationContextRunner { AnnotationConfigApplicationContext().apply { environment = NoSystemEnvironment() } }
            .withInitializer(ConfigDataApplicationContextInitializer())
            .withUserConfiguration(CoversConfig::class.java)
            .run { it.startupFailure?.message shouldContain "Could not bind properties to 'CoversProperties'" }
    }

    private class NoSystemEnvironment : StandardEnvironment() {
        override fun customizePropertySources(propertySources: MutablePropertySources) = Unit
    }
}
