package fr.amory.libris.library.infrastructure.web

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
)

@RestController
class MeController {
    @GetMapping("/api/v1/me")
    fun me(@AuthenticationPrincipal reader: Reader, authentication: Authentication): CurrentReaderResponse =
        CurrentReaderResponse(
            id = reader.id.value.toString(),
            username = reader.username,
            displayName = reader.displayName,
            email = reader.email,
            role = roleOf(authentication.authorities),
        )

    private fun roleOf(authorities: Collection<GrantedAuthority>): Role =
        if (authorities.any { it.authority == ADMIN_AUTHORITY }) Role.ADMIN else Role.READER
}
