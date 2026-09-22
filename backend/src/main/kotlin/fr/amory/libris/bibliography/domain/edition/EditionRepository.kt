package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Isbn

interface EditionRepository {
    fun insert(edition: Edition)

    fun findByIsbn(isbn: Isbn): Edition?
}
