package fr.amory.libris.infra.web

import fr.amory.libris.domain.Role
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class CurrentMemberResponse(
    val username: String,
    val displayName: String,
    val email: String,
    val role: Role,
)

@RestController
class MeController {
    @GetMapping("/api/v1/me")
    fun me(@AuthenticationPrincipal principal: MemberPrincipal): CurrentMemberResponse =
        CurrentMemberResponse(
            username = principal.member.username,
            displayName = principal.member.displayName,
            email = principal.member.email,
            role = principal.member.role,
        )
}
