package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Isbn

interface EditionRepository {
  fun insert(edition: Edition)

  fun update(edition: Edition)

  fun findByIsbn(isbn: Isbn): Edition?

  fun findByIds(ids: List<EditionId>): List<Edition>
}
