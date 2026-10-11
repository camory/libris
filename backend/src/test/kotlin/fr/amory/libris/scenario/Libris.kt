package fr.amory.libris.scenario

import com.jayway.jsonpath.JsonPath
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.Optional

class Libris(private val http: RestTestClient) {
  fun me(reader: Reader): RestTestClient.ResponseSpec =
    http.get()
      .uri("/api/v1/me")
      .headers { it.putAll(reader.headers) }
      .accept(APPLICATION_JSON)
      .exchange()

  fun defaultBookshelfOf(reader: Reader): String =
    JsonPath.read(bodyOf(me(reader).expectStatus().isOk()), "$.defaultBookshelf.id")

  fun ask(reader: Reader, isbn: String): RestTestClient.ResponseSpec =
    http.get()
      .uri("/api/v1/isbn/$isbn")
      .headers { it.putAll(reader.headers) }
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .exchange()

  fun add(reader: Reader, bookshelf: String, book: String): RestTestClient.ResponseSpec =
    http.post()
      .uri("/api/v1/bookshelves/$bookshelf/books")
      .headers { it.putAll(reader.headers) }
      .contentType(APPLICATION_JSON)
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .body(book)
      .exchange()

  fun catalogue(reader: Reader, after: String? = null): RestTestClient.ResponseSpec =
    http.get()
      .uri { it.path("/api/v1/books").queryParamIfPresent("after", Optional.ofNullable(after)).build() }
      .headers { it.putAll(reader.headers) }
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .exchange()
}

class Reader(username: String, displayName: String) {
  val headers = mapOf(
    "Remote-User" to listOf(username),
    "Remote-Name" to listOf(displayName),
    "Remote-Email" to listOf("$username@amory.fr"),
    "Remote-Groups" to listOf("family"),
  )
}

fun bodyOf(response: RestTestClient.ResponseSpec): String =
  requireNotNull(response.expectBody(String::class.java).returnResult().responseBody)
