package fr.amory.libris.web

import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverName
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CoverController(private val findCover: FindCover) {
  @GetMapping("/api/v1/covers/{name}")
  fun cover(@PathVariable("name") name: String): ResponseEntity<Any> {
    val coverName = CoverName.of(name) ?: return notACoverName().asResponse()
    return findCover(coverName)?.let(::pictureOf) ?: problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
  }

  private fun notACoverName(): ProblemDetail =
    problem(BAD_REQUEST, VALIDATION_PROBLEM).apply {
      setProperty("errors", listOf(ValidationErrorResponse(field = "name", code = "not-a-cover-name")))
    }

  private fun pictureOf(cover: Cover): ResponseEntity<Any> =
    ResponseEntity
      .ok()
      .contentType(parseMediaType(cover.mediaType))
      .header(CACHE_CONTROL, KEPT_A_YEAR)
      .body(cover.bytes)
}

private const val KEPT_A_YEAR = "public, max-age=31536000, immutable"
