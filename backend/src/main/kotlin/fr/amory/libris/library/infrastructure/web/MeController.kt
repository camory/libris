package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

enum class Role {
    READER,
    ADMIN,
}

data class CurrentReaderResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val role: Role,
    val defaultBookshelf: BookshelfResponse,
)

data class BookshelfResponse(
    val id: String,
    val name: String,
)

@RestController
class MeController(private val findDefaultBookshelf: FindDefaultBookshelf) {
    @GetMapping("/api/v1/me")
    fun me(@AuthenticationPrincipal reader: Reader, authentication: Authentication): CurrentReaderResponse =
        CurrentReaderResponse(
            id = reader.id.value.toString(),
            username = reader.username,
            displayName = reader.displayName,
            email = reader.email,
            role = roleOf(authentication.authorities),
            defaultBookshelf = responseOf(findDefaultBookshelf(reader)),
        )

    private fun responseOf(bookshelf: Bookshelf): BookshelfResponse =
        BookshelfResponse(bookshelf.id.value.toString(), bookshelf.name)

    private fun roleOf(authorities: Collection<GrantedAuthority>): Role =
        if (authorities.any { it.authority == ADMIN_AUTHORITY }) Role.ADMIN else Role.READER
}
