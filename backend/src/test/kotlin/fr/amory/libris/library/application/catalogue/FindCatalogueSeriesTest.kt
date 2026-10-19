package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.Found
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CatalogueEditionsAnswering
import fr.amory.libris.library.fixture.CopiesInMemory
import fr.amory.libris.library.fixture.bookshelfOf
import fr.amory.libris.library.fixture.copyOn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class FindCatalogueSeriesTest {
  private val lea = ReaderId.new()
  private val bookshelves = BookshelvesInMemory()
  private val copies = CopiesInMemory()
  private val editions = EditionsInMemory().also {
    it.insert(ROMANCE_DAWN)
    it.insert(BAGGY)
    it.insert(ASTERIX_LE_GAULOIS)
  }
  private val seriesRepository = SeriesInMemory().also { repository ->
    listOf(ONE_PIECE, ASTERIX).forEach { repository.findOrInsert(it) }
  }
  private val listCatalogueSeries =
    ListCatalogueSeries(CatalogueEditionsAnswering(), bookshelves, copies, editions, seriesRepository)
  private val findCatalogueSeries = FindCatalogueSeries(listCatalogueSeries)

  @Test
  fun `a series of the reader's catalogue is found with what the series list answers for it`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, ROMANCE_DAWN.id))
    copies.insert(copyOn(leasBookshelf, BAGGY.id))
    copies.insert(copyOn(leasBookshelf, ASTERIX_LE_GAULOIS.id))

    // When
    val result = findCatalogueSeries(lea, ONE_PIECE.id)

    // Then
    result.shouldBeInstanceOf<Found>().series shouldBe CatalogueSeries(ONE_PIECE.id, "One piece", 2, null, MANGA)
  }

  private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
    bookshelfOf(BookshelfId.new(), BookshelfName(name), memberships.toList()).also { bookshelves.insert(it) }
}

private val ONE_PIECE = Series(SeriesId.new(), "One piece")

private val ASTERIX = Series(SeriesId.new(), "Astérix")

private val ROMANCE_DAWN = Edition(
  id = EditionId.new(),
  isbn = null,
  kind = MANGA,
  title = "Romance dawn",
  subtitle = null,
  contributions = Contributions.of(emptyList()),
  seriesVolume = SeriesVolume(ONE_PIECE.id, 1),
  collection = null,
  publisher = null,
  publicationYear = null,
  language = null,
  pageCount = null,
  summary = null,
  coverName = null,
)

private val BAGGY = ROMANCE_DAWN.copy(
  id = EditionId.new(),
  title = "Aux prises avec Baggy",
  seriesVolume = SeriesVolume(ONE_PIECE.id, 2),
)

private val ASTERIX_LE_GAULOIS = ROMANCE_DAWN.copy(
  id = EditionId.new(),
  kind = BD,
  title = "Astérix le Gaulois",
  seriesVolume = SeriesVolume(ASTERIX.id, 1),
)
