package fr.amory.libris.web

fun interface RequestPrincipal {
  fun of(username: String, email: String, displayName: String): Any
}
