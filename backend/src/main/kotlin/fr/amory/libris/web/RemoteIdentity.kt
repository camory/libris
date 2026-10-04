package fr.amory.libris.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import kotlin.text.Charsets.ISO_8859_1
import kotlin.text.Charsets.UTF_8

private const val ADMIN_GROUP = "libris-admin"

const val READER_AUTHORITY = "ROLE_READER"
const val ADMIN_AUTHORITY = "ROLE_ADMIN"

class RemoteIdentity private constructor(
  val username: String,
  val email: String,
  val displayName: String,
  val authorities: List<GrantedAuthority>) {
  companion object {
    fun of(request: HttpServletRequest): RemoteIdentity? {
      val username = request.getHeader("Remote-User")
      val email = request.getHeader("Remote-Email")?.takeUnless { it.isBlank() }
      if (username == null || email == null) return null
      return RemoteIdentity(
        username = username,
        email = email,
        displayName = request.getHeader("Remote-Name")?.let(::utf8)?.takeUnless { it.isBlank() } ?: username,
        authorities = authoritiesOf(request.getHeader("Remote-Groups").orEmpty().split(",").map { it.trim() }),
      )
    }

    private fun authoritiesOf(groups: List<String>): List<GrantedAuthority> =
      buildList {
        add(SimpleGrantedAuthority(READER_AUTHORITY))
        if (ADMIN_GROUP in groups) add(SimpleGrantedAuthority(ADMIN_AUTHORITY))
      }

    private fun utf8(header: String): String =
      String(header.toByteArray(ISO_8859_1), UTF_8)
  }
}
