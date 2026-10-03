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
        displayName = request.getHeader("Remote-Name").orEmpty(),
      )
  }
}
