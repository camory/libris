package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.application.edition.EditEdition
import fr.amory.libris.bibliography.application.edition.EditionDetails
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import fr.amory.libris.library.application.ReaderBookshelf
import fr.amory.libris.library.application.catalogue.SaveEditionResult.Saved
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CopiesInMemory
import fr.amory.libris.library.fixture.bookshelfOf
import fr.amory.libris.library.fixture.copyOn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction

class SaveEditionTest {
  private val lea = ReaderId.new()
  private val sam = ReaderId.new()
  private val bookshelves = BookshelvesInMemory()
  private val copies = CopiesInMemory()
  private val editions = EditionsInMemory().also { it.insert(ONE_PIECE_2_HELD) }
  private val seriesRepository = SeriesInMemory()
  private val saveEdition =
    SaveEdition(EditEdition(editions, seriesRepository, withoutTransaction()), bookshelves, copies, seriesRepository)

  @Test
  fun `an edition whose copies sit on another reader's bookshelf alone is answered with none`() {
    // Given
    val samsBookshelf = bookshelfOf("Bibliothèque de Sam", Membership(sam, OWNER))
    copies.insert(copyOn(samsBookshelf, ONE_PIECE_2))

    // When
    val result = saveEdition(lea, ONE_PIECE_2, asEdited())

    // Then
    result shouldBe Saved(HeldEdition(editions.stored.single(), null, emptyList()))
    editions.stored.single().title shouldBe "Aux prises avec Baggy et ses hommes"
  }

  @Test
  fun `a copy on a bookshelf the reader belongs to is answered as on the ouvrage's page`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    val samsBookshelf = bookshelfOf("Bibliothèque de Sam", Membership(sam, OWNER))
    val leasCopy = copyOn(leasBookshelf, ONE_PIECE_2).also { copies.insert(it) }
    copies.insert(copyOn(samsBookshelf, ONE_PIECE_2))

    // When
    val result = saveEdition(lea, ONE_PIECE_2, asEdited())

    // Then
    result.shouldBeInstanceOf<Saved>().held.copies shouldBe listOf(
      CopyOnBookshelf(leasCopy.id, ReaderBookshelf(leasBookshelf.id, "Bibliothèque de Léa", OWNER)),
    )
  }

  @Test
  fun `a series sent in another capitalisation is answered under the house's spelling`() {
    // Given
    seriesRepository.findOrInsert(Series(SeriesId.new(), "One piece"))

    // When
    val result = saveEdition(lea, ONE_PIECE_2, asEdited().copy(series = SeriesEntry("ONE PIECE", 2)))

    // Then
    result.shouldBeInstanceOf<Saved>().held.series shouldBe SeriesEntry("One piece", 2)
  }

  private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
    bookshelfOf(BookshelfId.new(), BookshelfName(name), memberships.toList()).also { bookshelves.insert(it) }

  private fun asEdited(): EditionDetails =
    EditionDetails(
      kind = MANGA,
      title = "Aux prises avec Baggy et ses hommes",
      subtitle = null,
      contributions = Contributions.of(
        listOf(Contribution("Eiichirō Oda", WRITER), Contribution("Eiichirō Oda", ARTIST)),
      ),
      series = null,
      collection = "Shonen manga",
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 208,
      summary = "Luffy et Zoro affrontent le pirate Baggy le clown.",
    )
}

private val ONE_PIECE_2 = EditionId.new()

private val ONE_PIECE_2_HELD = Edition(
  id = ONE_PIECE_2,
  isbn = null,
  kind = MANGA,
  title = "Aux prises avec Baggy",
  subtitle = null,
  contributions = Contributions.of(emptyList()),
  seriesVolume = null,
  collection = null,
  publisher = null,
  publicationYear = null,
  language = null,
  pageCount = null,
  summary = null,
  coverName = null,
)
