package fr.amory.libris.web

import fr.amory.libris.library.application.AddBookResult
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddBookResult.NotAnOwner
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.web.NewBookValidation.Accepted
import fr.amory.libris.web.NewBookValidation.Refused
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

  private fun invalid(errors: List<ValidationErrorResponse>): ProblemDetail =
    problem(BAD_REQUEST, VALIDATION_PROBLEM).apply { setProperty("errors", errors) }

  private fun responseOf(result: AddBookResult): ResponseEntity<Any> =
    when (result) {
      is Added                    -> ResponseEntity.status(CREATED).body(CopyResponse.from(result))
      NoSuchBookshelf, NotAnOwner -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
    }
}

data class CopyResponse(
  val id: String,
  val bookshelf: BookshelfResponse) {
  companion object {
    fun from(copy: CopyOnBookshelf): CopyResponse =
      CopyResponse(copy.copyId.value.toString(), BookshelfResponse.from(copy))

    fun from(added: Added): CopyResponse =
      CopyResponse(added.copy.id.value.toString(), BookshelfResponse.from(added.bookshelf))
  }
}

data class BookshelfResponse(
  val id: String,
  val name: String) {
  companion object {
    fun from(bookshelf: Bookshelf): BookshelfResponse =
      BookshelfResponse(bookshelf.id.value.toString(), bookshelf.name)

    fun from(copy: CopyOnBookshelf): BookshelfResponse =
      BookshelfResponse(copy.bookshelfId.value.toString(), copy.bookshelfName)
  }
}
