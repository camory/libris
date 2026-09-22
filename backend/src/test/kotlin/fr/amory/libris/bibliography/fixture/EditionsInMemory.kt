package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionRepository

class EditionsInMemory : EditionRepository {
    private val editions = mutableListOf<Edition>()

    val stored: List<Edition> get() = editions.toList()

    override fun insert(edition: Edition) {
        editions += edition
    }

    override fun findByIsbn(isbn: Isbn): Edition? = editions.find { it.isbn == isbn }
}
