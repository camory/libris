package fr.amory.libris.library.infrastructure.web

import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ProblemAdvice {
    @ExceptionHandler(TypeMismatchException::class, HttpMessageNotReadableException::class)
    fun validation(): ResponseEntity<Any> = problem(BAD_REQUEST, VALIDATION_PROBLEM).asResponse()
}
