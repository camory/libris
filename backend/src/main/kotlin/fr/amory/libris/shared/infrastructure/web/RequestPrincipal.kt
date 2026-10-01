package fr.amory.libris.shared.infrastructure.web

fun interface RequestPrincipal {
  fun of(username: String, email: String, displayName: String): Any
}
