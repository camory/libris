package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.AwaitedCoverRepository
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations

@Service
class HoldEdition(
  private val editions: EditionRepository,
  private val seriesRepository: SeriesRepository,
  private val awaitedCovers: AwaitedCoverRepository,
  private val transactions: TransactionOperations,
  private val editEdition: EditEdition) {
  operator fun invoke(newEdition: NewEdition): Edition =
    newEdition.isbn?.let { editions.findByIsbn(it) }?.let { written(it, newEdition) } ?: inserted(newEdition)

  private fun written(held: Edition, newEdition: NewEdition): Edition =
    checkNotNull(editEdition(held.id, newEdition.details) as? Edited).edition

  private fun inserted(newEdition: NewEdition): Edition =
    checkNotNull(
      transactions.execute {
        val details = newEdition.details
        val edition = editionOf(newEdition.isbn, details, details.series?.let { seriesVolumeOf(it) })
        editions.insert(edition)
        edition.isbn?.let { awaitedCovers.insert(AwaitedCover(it, newEdition.coverSource)) }
        edition
      },
    )

  private fun seriesVolumeOf(seriesEntry: SeriesEntry): SeriesVolume =
    SeriesVolume(
      seriesRepository.findOrInsert(Series(SeriesId.new(), seriesEntry.name)).id,
      seriesEntry.volumeNumber,
    )

  private fun editionOf(isbn: Isbn?, details: EditionDetails, seriesVolume: SeriesVolume?): Edition =
    Edition(
      id = EditionId.new(),
      isbn = isbn,
      kind = details.kind,
      title = details.title,
      subtitle = details.subtitle,
      contributions = details.contributions,
      seriesVolume = seriesVolume,
      collection = details.collection,
      publisher = details.publisher,
      publicationYear = details.publicationYear,
      language = details.language,
      pageCount = details.pageCount,
      summary = details.summary,
      coverName = null,
    )
}
