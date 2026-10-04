package fr.amory.libris

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["LIBRIS_COVERS_DIR=build/test-covers"])
@AutoConfigureRestTestClient
class LibrisApplicationTest @Autowired constructor(private val client: RestTestClient) {
  @Test
  fun `the application starts and reports itself healthy`() {
    client.get()
      .uri("/actuator/health")
      .exchange()
      .expectStatus().isOk()
      .expectBody().jsonPath("$.status").isEqualTo("UP")
  }
}
