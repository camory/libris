package fr.amory.libris.bibliography.application.edition

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
  private val transactions: TransactionOperations) {
  operator fun invoke(newEdition: NewEdition): Edition =
    newEdition.isbn?.let { editions.findByIsbn(it) } ?: inserted(newEdition)

  private fun inserted(newEdition: NewEdition): Edition =
    checkNotNull(
      transactions.execute {
        val edition = editionOf(newEdition, newEdition.series?.let { seriesVolumeOf(it) })
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

  private fun editionOf(newEdition: NewEdition, seriesVolume: SeriesVolume?): Edition =
    Edition(
      id = EditionId.new(),
      isbn = newEdition.isbn,
      kind = newEdition.kind,
      title = newEdition.title,
      subtitle = newEdition.subtitle,
      contributions = newEdition.contributions,
      seriesVolume = seriesVolume,
      collection = newEdition.collection,
      publisher = newEdition.publisher,
      publicationYear = newEdition.publicationYear,
      language = newEdition.language,
      pageCount = newEdition.pageCount,
      summary = newEdition.summary,
      coverName = null,
    )
}
