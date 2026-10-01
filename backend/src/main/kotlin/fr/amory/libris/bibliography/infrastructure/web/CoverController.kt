package fr.amory.libris.bibliography.infrastructure.web

import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.shared.infrastructure.web.NOT_FOUND_PROBLEM
import fr.amory.libris.shared.infrastructure.web.asResponse
import fr.amory.libris.shared.infrastructure.web.problem
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

private const val KEPT_A_YEAR = "public, max-age=31536000, immutable"

@RestController
class CoverController(private val findCover: FindCover) {
    @GetMapping("/api/v1/covers/{name}")
    fun cover(@PathVariable("name") name: String): ResponseEntity<Any> {
        val cover = CoverName.of(name)?.let { findCover(it) }
            ?: return problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
        return ResponseEntity.ok()
            .contentType(parseMediaType(cover.mediaType))
            .header(CACHE_CONTROL, KEPT_A_YEAR)
            .body(cover.bytes)
    }
}
