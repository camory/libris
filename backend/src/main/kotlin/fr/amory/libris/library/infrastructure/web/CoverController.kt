package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.bibliography.domain.cover.CoverName
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.IMAGE_JPEG
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

private const val KEPT_A_YEAR = "public, max-age=31536000, immutable"

@RestController
class CoverController(private val findCover: FindCover) {
    @GetMapping("/api/v1/covers/{name}")
    fun cover(@PathVariable("name") name: String): ResponseEntity<Any> {
        val cover = findCover(CoverName(name)) ?: return problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
        return ResponseEntity.ok()
            .contentType(IMAGE_JPEG)
            .header(CACHE_CONTROL, KEPT_A_YEAR)
            .body(cover.bytes)
    }
}
