package fr.amory.libris.infra.web

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

enum class Role {
    READER,
    ADMIN,
}

data class CurrentReaderResponse(
    val username: String,
    val displayName: String,
    val email: String,
    val role: Role,
)

@RestController
class MeController {
    @GetMapping("/api/v1/me")
    fun me(@AuthenticationPrincipal principal: ReaderPrincipal): CurrentReaderResponse =
        CurrentReaderResponse(
            username = principal.reader.username,
            displayName = principal.reader.displayName,
            email = principal.reader.email,
            role = roleOf(principal.authorities),
        )

    private fun roleOf(authorities: Collection<GrantedAuthority>): Role =
        if (authorities.any { it.authority == ADMIN_AUTHORITY }) Role.ADMIN else Role.READER
}
