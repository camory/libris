package fr.amory.libris.web.security

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
      val username = textOf(request, "Remote-User")
      val email = textOf(request, "Remote-Email")
      if (username == null || email == null) return null
      return RemoteIdentity(
        username = username,
        email = email,
        displayName = textOf(request, "Remote-Name") ?: username,
        authorities = authoritiesOf(textOf(request, "Remote-Groups").orEmpty()),
      )
    }

    private fun textOf(request: HttpServletRequest, header: String): String? =
      request.getHeader(header)?.let(::utf8)?.takeUnless { it.isBlank() }

    private fun authoritiesOf(groups: String): List<GrantedAuthority> =
      buildList {
        add(SimpleGrantedAuthority(READER_AUTHORITY))
        if (ADMIN_GROUP in groups.split(",").map { it.trim() }) add(SimpleGrantedAuthority(ADMIN_AUTHORITY))
      }
  }
}

fun utf8(header: String): String =
  String(header.toByteArray(ISO_8859_1), UTF_8)
