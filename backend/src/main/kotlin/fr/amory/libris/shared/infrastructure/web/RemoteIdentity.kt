package fr.amory.libris.shared.infrastructure.web

import jakarta.servlet.http.HttpServletRequest

class RemoteIdentity private constructor(val username: String, val email: String) {
  companion object {
    fun of(request: HttpServletRequest): RemoteIdentity? =
      RemoteIdentity(request.getHeader("Remote-User"), request.getHeader("Remote-Email"))
  }
}
