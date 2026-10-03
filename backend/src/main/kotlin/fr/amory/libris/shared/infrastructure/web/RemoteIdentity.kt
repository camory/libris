package fr.amory.libris.shared.infrastructure.web

import jakarta.servlet.http.HttpServletRequest

class RemoteIdentity private constructor(
  val username: String,
  val email: String,
  val displayName: String) {
  companion object {
    fun of(request: HttpServletRequest): RemoteIdentity? =
      RemoteIdentity(
        username = request.getHeader("Remote-User"),
        email = request.getHeader("Remote-Email"),
        displayName = utf8(request.getHeader("Remote-Name").orEmpty()),
      )

    private fun utf8(header: String): String =
      String(header.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
  }
}
