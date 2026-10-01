package fr.amory.libris.library.domain.copy

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.reader.ReaderId

interface CatalogueEditions {
  fun findPage(readerId: ReaderId, after: EditionId?, size: Int): EditionIdPage
}
