package fr.amory.libris.library.fixture

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.copy.CatalogueEditions
import fr.amory.libris.library.domain.copy.EditionIdPage
import fr.amory.libris.library.domain.reader.ReaderId

class CatalogueEditionsAnswering(private val answer: EditionIdPage) : CatalogueEditions {
  private val pagesAsked = mutableListOf<Triple<ReaderId, EditionId?, Int>>()

  val asked: List<Triple<ReaderId, EditionId?, Int>> get() = pagesAsked.toList()

  override fun findPage(readerId: ReaderId, after: EditionId?, size: Int): EditionIdPage {
    pagesAsked += Triple(readerId, after, size)
    return answer
  }
}
