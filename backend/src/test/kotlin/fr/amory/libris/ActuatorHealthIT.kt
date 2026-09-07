package fr.amory.libris

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorHealthIT(private val rest: TestRestTemplate, @param:LocalServerPort private val port: Int) {

    @Test
    fun `GET actuator health answers UP`() {
        val response = rest.getForEntity("http://localhost:$port/actuator/health", Map::class.java)

        response.statusCode shouldBe HttpStatus.OK
        response.body?.get("status") shouldBe "UP"
    }
}
