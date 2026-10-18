package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.AwaitedCoverRepository
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import org.springframework.stereotype.Service

@Service
class HoldEdition(
  private val editions: EditionRepository,
  private val awaitedCovers: AwaitedCoverRepository) {
  operator fun invoke(newEdition: NewEdition): Edition =
    newEdition.isbn?.let { editions.findByIsbn(it) } ?: inserted(newEdition)

  private fun inserted(newEdition: NewEdition): Edition {
    val edition = editionOf(newEdition)
    editions.insert(edition)
    edition.isbn?.let { awaitedCovers.insert(AwaitedCover(it, newEdition.coverSource)) }
    return edition
  }

  private fun editionOf(newEdition: NewEdition): Edition =
    Edition(
      id = EditionId.new(),
      isbn = newEdition.isbn,
      kind = newEdition.kind,
      title = newEdition.title,
      subtitle = newEdition.subtitle,
      contributions = newEdition.contributions,
      series = newEdition.series,
      collection = newEdition.collection,
      publisher = newEdition.publisher,
      publicationYear = newEdition.publicationYear,
      language = newEdition.language,
      pageCount = newEdition.pageCount,
      summary = newEdition.summary,
      coverName = null,
    )
}
