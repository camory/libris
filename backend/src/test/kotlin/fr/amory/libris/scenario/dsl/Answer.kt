package fr.amory.libris.scenario.dsl

import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.web.servlet.client.EntityExchangeResult
import org.springframework.test.web.servlet.client.RestTestClient

class Answer<T>(response: RestTestClient.ResponseSpec, private val read: (String) -> T) {
  private val result = response.expectBody(String::class.java).returnResult()

  val refusal: Refusal? =
    if (result.status.is2xxSuccessful) null else refusalOf(result)

  val value: T
    get() {
      check(refusal == null) { "Libris refused: $refusal" }
      return read(result.responseBody.orEmpty())
    }
}

data class Refusal(val status: Int, val type: String, val fields: List<String> = emptyList())

val NOT_FOUND = Refusal(404, "/problems/not-found")

val CONFLICT = Refusal(409, "/problems/conflict")

fun invalid(vararg fields: String) =
  Refusal(400, "/problems/validation", fields.toList())

data class Problem(val type: String, val errors: List<FieldError>?)

data class FieldError(val field: String)

private fun refusalOf(result: EntityExchangeResult<String>): Refusal {
  check(APPLICATION_PROBLEM_JSON.isCompatibleWith(result.responseHeaders.contentType)) {
    "not a problem: ${result.status} ${result.responseBody}"
  }
  val problem = read<Problem>(requireNotNull(result.responseBody))
  return Refusal(result.status.value(), problem.type, problem.errors.orEmpty().map { it.field })
}
