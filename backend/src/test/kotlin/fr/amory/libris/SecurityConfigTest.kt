package fr.amory.libris

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class SecurityConfigTest @Autowired constructor(
    private val client: RestTestClient,
) {
    @Test
    fun `a request without the identity headers is refused`() {
        client.get()
            .uri("/api/v1/me")
            .exchange()
            .expectStatus().isForbidden()
    }

    @Test
    fun `a request whose user header is missing is refused`() {
        client.get()
            .uri("/api/v1/me")
            .header("Remote-Name", "Juliette")
            .header("Remote-Email", "juliette@amory.fr")
            .header("Remote-Groups", "family")
            .exchange()
            .expectStatus().isForbidden()
    }

    @Test
    fun `a request whose email header is blank is refused`() {
        client.get()
            .uri("/api/v1/me")
            .header("Remote-User", "juliette")
            .header("Remote-Name", "Juliette")
            .header("Remote-Email", " ")
            .header("Remote-Groups", "family")
            .exchange()
            .expectStatus().isForbidden()
    }

    @Test
    fun `a request whose email header is missing is refused`() {
        client.get()
            .uri("/api/v1/me")
            .header("Remote-User", "juliette")
            .header("Remote-Name", "Juliette")
            .header("Remote-Groups", "family")
            .exchange()
            .expectStatus().isForbidden()
    }

    @Test
    fun `a write without the X-Requested-With header is refused`() {
        client.post()
            .uri("/api/v1/me")
            .header("Remote-User", "tophe")
            .header("Remote-Name", "Tophe")
            .header("Remote-Email", "tophe@amory.fr")
            .header("Remote-Groups", "family,libris-admin")
            .exchange()
            .expectStatus().isForbidden()
    }

    @Test
    fun `a write carrying the X-Requested-With header reaches the application`() {
        client.post()
            .uri("/api/v1/me")
            .header("Remote-User", "tophe")
            .header("Remote-Name", "Tophe")
            .header("Remote-Email", "tophe@amory.fr")
            .header("Remote-Groups", "family,libris-admin")
            .header("X-Requested-With", "XMLHttpRequest")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
    }

    @Test
    fun `the health endpoint answers without any header`() {
        client.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk()
    }
}
