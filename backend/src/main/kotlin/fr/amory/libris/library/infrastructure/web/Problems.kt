package fr.amory.libris.library.infrastructure.web

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import java.net.URI

const val VALIDATION_PROBLEM = "/problems/validation"
const val NOT_FOUND_PROBLEM = "/problems/not-found"

data class ValidationErrorResponse(
    val field: String,
    val code: String,
)

fun problem(status: HttpStatus, type: String): ProblemDetail =
    ProblemDetail.forStatus(status).apply {
        this.type = URI.create(type)
    }

fun ProblemDetail.asResponse(): ResponseEntity<Any> = ResponseEntity.status(status).body(this)
