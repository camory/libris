package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.library.application.AddBookResult
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddBookResult.NotAnOwner
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.infrastructure.web.NewBookValidation.Accepted
import fr.amory.libris.library.infrastructure.web.NewBookValidation.Refused
import fr.amory.libris.shared.infrastructure.web.NOT_FOUND_PROBLEM
import fr.amory.libris.shared.infrastructure.web.VALIDATION_PROBLEM
import fr.amory.libris.shared.infrastructure.web.ValidationErrorResponse
import fr.amory.libris.shared.infrastructure.web.asResponse
import fr.amory.libris.shared.infrastructure.web.problem
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class BookshelfResponse(
  val id: String,
  val name: String)

data class CopyResponse(
  val id: String,
  val bookshelf: BookshelfResponse)

@RestController
class BookshelfController(private val addBookToBookshelf: AddBookToBookshelf) {
  @PostMapping("/api/v1/bookshelves/{id}/books")
  fun add(
    @AuthenticationPrincipal reader: Reader,
    @PathVariable("id") id: UUID,
    @RequestBody request: NewBookRequest): ResponseEntity<Any> =
    when (val validation = request.validate()) {
      is Refused  -> invalid(validation.errors).asResponse()
      is Accepted -> responseOf(addBookToBookshelf(reader.id, BookshelfId(id), validation.book))
    }

  private fun responseOf(result: AddBookResult): ResponseEntity<Any> =
    when (result) {
      is Added                    -> created(result)
      NoSuchBookshelf, NotAnOwner -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
    }

  private fun created(added: Added): ResponseEntity<Any> =
    ResponseEntity
      .status(CREATED)
      .body(
        CopyResponse(
          id = added.copy.id.value.toString(),
          bookshelf = BookshelfResponse(added.bookshelf.id.value.toString(), added.bookshelf.name),
        ),
      )

  private fun invalid(errors: List<ValidationErrorResponse>): ProblemDetail =
    problem(BAD_REQUEST, VALIDATION_PROBLEM).apply { setProperty("errors", errors) }
}
