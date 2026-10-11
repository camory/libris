package fr.amory.libris.web.me

import fr.amory.libris.library.application.ChooseDefaultBookshelf
import fr.amory.libris.library.application.ChooseDefaultResult.Chosen
import fr.amory.libris.library.application.ChooseDefaultResult.NoSuchBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.web.bookshelf.BookshelfResponse
import fr.amory.libris.web.me.Role.ADMIN
import fr.amory.libris.web.me.Role.READER
import fr.amory.libris.web.problem.NOT_FOUND_PROBLEM
import fr.amory.libris.web.problem.asResponse
import fr.amory.libris.web.problem.problem
import fr.amory.libris.web.security.ADMIN_AUTHORITY
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MeController(
  private val findDefaultBookshelf: FindDefaultBookshelf,
  private val chooseDefaultBookshelf: ChooseDefaultBookshelf) {
  @GetMapping("/api/v1/me")
  fun me(@AuthenticationPrincipal reader: Reader, authentication: Authentication): CurrentReaderResponse =
    CurrentReaderResponse.from(reader, findDefaultBookshelf(reader), authentication.authorities)

  @PutMapping("/api/v1/me/default-bookshelf")
  fun chooseDefault(
    @AuthenticationPrincipal reader: Reader,
    @RequestBody request: DefaultBookshelfRequest): ResponseEntity<Any> =
    when (chooseDefaultBookshelf(reader, BookshelfId(request.id))) {
      Chosen          -> ResponseEntity.status(NO_CONTENT).build()
      NoSuchBookshelf -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
    }
}

data class CurrentReaderResponse(
  val id: String,
  val username: String,
  val displayName: String,
  val email: String,
  val role: Role,
  val defaultBookshelf: BookshelfResponse) {
  companion object {
    fun from(
      reader: Reader,
      defaultBookshelf: Bookshelf,
      authorities: Collection<GrantedAuthority>): CurrentReaderResponse =
      CurrentReaderResponse(
        id = reader.id.value.toString(),
        username = reader.username,
        displayName = reader.displayName,
        email = reader.email,
        role = if (authorities.any { it.authority == ADMIN_AUTHORITY }) ADMIN else READER,
        defaultBookshelf = BookshelfResponse.from(defaultBookshelf),
      )
  }
}

enum class Role {
  READER,
  ADMIN
}
