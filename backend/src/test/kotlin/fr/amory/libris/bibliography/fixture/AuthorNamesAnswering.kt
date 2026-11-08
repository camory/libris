package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.edition.AuthorNames

class AuthorNamesAnswering(private val names: List<String>) : AuthorNames {
  override fun findAll(): List<String> =
    names
}
