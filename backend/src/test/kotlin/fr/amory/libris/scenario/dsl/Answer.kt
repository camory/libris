package fr.amory.libris.scenario.dsl

import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.web.servlet.client.EntityExchangeResult
import org.springframework.test.web.servlet.client.RestTestClient

class Answer<T>(response: RestTestClient.ResponseSpec, private val read: (EntityExchangeResult<ByteArray>) -> T) {
  private val result = response.expectBody(ByteArray::class.java).returnResult()

  val refusal: Refusal? =
    if (result.status.is2xxSuccessful) null else refusalOf(result)

  val value: T
    get() {
      check(refusal == null) { "Libris refused: $refusal" }
      return read(result)
    }
}

data class Refusal(val status: Int, val type: String, val fields: List<String> = emptyList())

val NOT_FOUND = Refusal(404, "/problems/not-found")

val CONFLICT = Refusal(409, "/problems/conflict")

val SOURCES_UNAVAILABLE = Refusal(503, "/problems/sources-unavailable")

fun invalid(vararg fields: String) =
  Refusal(400, "/problems/validation", fields.toList())

data class Problem(val type: String, val errors: List<FieldError>?)

data class FieldError(val field: String)

fun textOf(result: EntityExchangeResult<ByteArray>): String =
  result.responseBody?.toString(Charsets.UTF_8).orEmpty()

private fun refusalOf(result: EntityExchangeResult<ByteArray>): Refusal {
  check(APPLICATION_PROBLEM_JSON.isCompatibleWith(result.responseHeaders.contentType)) {
    "not a problem: ${result.status} ${textOf(result)}"
  }
  val problem = read<Problem>(textOf(result))
  return Refusal(result.status.value(), problem.type, problem.errors.orEmpty().map { it.field })
}
