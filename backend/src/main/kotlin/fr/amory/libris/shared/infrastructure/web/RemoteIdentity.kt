package fr.amory.libris.shared.infrastructure.web

import jakarta.servlet.http.HttpServletRequest

class RemoteIdentity private constructor(
  val username: String,
  val email: String,
  val displayName: String) {
  companion object {
    fun of(request: HttpServletRequest): RemoteIdentity? {
      val username = request.getHeader("Remote-User")
      return RemoteIdentity(
        username = username,
        email = request.getHeader("Remote-Email"),
        displayName = request.getHeader("Remote-Name")?.let(::utf8)?.takeUnless { it.isBlank() } ?: username,
      )
    }

    private fun utf8(header: String): String =
      String(header.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
  }
}
