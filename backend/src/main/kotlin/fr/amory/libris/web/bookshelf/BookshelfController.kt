package fr.amory.libris.web.bookshelf

import fr.amory.libris.library.application.AddBookResult
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddBookResult.NotAnOwner
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.AddMemberResult
import fr.amory.libris.library.application.AddMemberResult.AlreadyAMember
import fr.amory.libris.library.application.AddMemberResult.UnknownReader
import fr.amory.libris.library.application.AddMemberToBookshelf
import fr.amory.libris.library.application.BookshelfMember
import fr.amory.libris.library.application.CreateBookshelf
import fr.amory.libris.library.application.CreateBookshelfResult.BlankName
import fr.amory.libris.library.application.CreateBookshelfResult.Created
import fr.amory.libris.library.application.CreateBookshelfResult.NameTaken
import fr.amory.libris.library.application.FindBookshelf
import fr.amory.libris.library.application.FindBookshelfResult
import fr.amory.libris.library.application.FindBookshelfResult.Found
import fr.amory.libris.library.application.ListReaderBookshelves
import fr.amory.libris.library.application.ReaderBookshelf
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.MembershipRole
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.web.bookshelf.NewBookValidation.Accepted
import fr.amory.libris.web.bookshelf.NewBookValidation.Refused
import fr.amory.libris.web.problem.CONFLICT_PROBLEM
import fr.amory.libris.web.problem.NOT_FOUND_PROBLEM
import fr.amory.libris.web.problem.VALIDATION_PROBLEM
import fr.amory.libris.web.problem.ValidationErrorResponse
import fr.amory.libris.web.problem.asResponse
import fr.amory.libris.web.problem.problem
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class BookshelfController(
  private val listReaderBookshelves: ListReaderBookshelves,
  private val createBookshelf: CreateBookshelf,
  private val findBookshelf: FindBookshelf,
  private val addBookToBookshelf: AddBookToBookshelf,
  private val addMemberToBookshelf: AddMemberToBookshelf) {
  @GetMapping("/api/v1/bookshelves")
  fun list(@AuthenticationPrincipal reader: Reader): List<ReaderBookshelfResponse> =
    listReaderBookshelves(reader).map { ReaderBookshelfResponse.from(it) }

  @PostMapping("/api/v1/bookshelves")
  fun create(@AuthenticationPrincipal reader: Reader, @RequestBody request: NewBookshelfRequest): ResponseEntity<Any> =
    when (val result = createBookshelf(reader.id, request.name)) {
      is Created -> ResponseEntity.status(CREATED).body(BookshelfResponse.from(result.bookshelf))
      BlankName  -> invalid(listOf(ValidationErrorResponse("name", "blank"))).asResponse()
      NameTaken  -> problem(CONFLICT, CONFLICT_PROBLEM).asResponse()
    }

  @GetMapping("/api/v1/bookshelves/{id}")
  fun find(@AuthenticationPrincipal reader: Reader, @PathVariable("id") id: UUID): ResponseEntity<Any> =
    when (val result = findBookshelf(reader.id, BookshelfId(id))) {
      is Found                            -> ResponseEntity.status(OK).body(BookshelfWithMembersResponse.from(result))
      FindBookshelfResult.NoSuchBookshelf -> notFound()
    }

  @PostMapping("/api/v1/bookshelves/{id}/books")
  fun add(
    @AuthenticationPrincipal reader: Reader,
    @PathVariable("id") id: UUID,
    @RequestBody request: NewBookRequest): ResponseEntity<Any> =
    when (val validation = request.validate()) {
      is Refused  -> invalid(validation.errors).asResponse()
      is Accepted -> responseOf(addBookToBookshelf(reader.id, BookshelfId(id), validation.book))
    }

  @PostMapping("/api/v1/bookshelves/{id}/members")
  fun addMember(
    @AuthenticationPrincipal reader: Reader,
    @PathVariable("id") id: UUID,
    @RequestBody request: NewMemberRequest): ResponseEntity<Any> =
    when (val result = addMemberToBookshelf(reader.id, BookshelfId(id), request.toMembership())) {
      is AddMemberResult.Added        -> ResponseEntity.status(CREATED).body(MemberResponse.from(result.member))
      AddMemberResult.NoSuchBookshelf -> notFound()
      UnknownReader                   -> invalid(listOf(ValidationErrorResponse("readerId", "unknown"))).asResponse()
      AlreadyAMember                  -> problem(CONFLICT, CONFLICT_PROBLEM).asResponse()
    }

  private fun invalid(errors: List<ValidationErrorResponse>): ProblemDetail =
    problem(BAD_REQUEST, VALIDATION_PROBLEM).apply { setProperty("errors", errors) }

  private fun notFound(): ResponseEntity<Any> =
    problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()

  private fun responseOf(result: AddBookResult): ResponseEntity<Any> =
    when (result) {
      is Added                    -> ResponseEntity.status(CREATED).body(CopyResponse.from(result))
      NoSuchBookshelf, NotAnOwner -> notFound()
    }
}

data class ReaderBookshelfResponse(
  val id: String,
  val name: String,
  val role: MembershipRole) {
  companion object {
    fun from(bookshelf: ReaderBookshelf): ReaderBookshelfResponse =
      ReaderBookshelfResponse(bookshelf.id.value.toString(), bookshelf.name, bookshelf.role)
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

data class BookshelfWithMembersResponse(
  val id: String,
  val name: String,
  val members: List<MemberResponse>) {
  companion object {
    fun from(found: Found): BookshelfWithMembersResponse =
      BookshelfWithMembersResponse(
        found.bookshelf.id.value.toString(),
        found.bookshelf.name,
        found.members.map { MemberResponse.from(it) },
      )
  }
}

data class MemberResponse(
  val id: String,
  val displayName: String,
  val role: MembershipRole) {
  companion object {
    fun from(member: BookshelfMember): MemberResponse =
      MemberResponse(member.readerId.value.toString(), member.displayName, member.role)
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
