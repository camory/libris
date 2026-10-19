package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.Found
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.NoSuchSeries
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CatalogueEditionsAnswering
import fr.amory.libris.library.fixture.CopiesInMemory
import fr.amory.libris.library.fixture.bookshelfOf
import fr.amory.libris.library.fixture.copyOn
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class FindCatalogueSeriesTest {
  private val lea = ReaderId.new()
  private val sam = ReaderId.new()
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
  private val catalogueEditions = CatalogueEditionsAnswering(firstOfEachSeries = setOf(ROMANCE_DAWN.id))
  private val findCatalogueSeries =
    FindCatalogueSeries(catalogueEditions, bookshelves, copies, editions, seriesRepository)

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
    result.shouldBeInstanceOf<Found>().series shouldBe
      CatalogueSeries(ONE_PIECE.id, "One piece", 2, ROMANCE_DAWN_COVER, MANGA)
  }

  @Test
  fun `the series answers its editions on the reader's bookshelves, no other series'`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    val copy = copyOn(leasBookshelf, ROMANCE_DAWN.id).also { copies.insert(it) }
    copies.insert(copyOn(leasBookshelf, ASTERIX_LE_GAULOIS.id))

    // When
    val result = findCatalogueSeries(lea, ONE_PIECE.id)

    // Then
    result.shouldBeInstanceOf<Found>().books shouldBe listOf(
      HeldEdition(
        ROMANCE_DAWN,
        SeriesEntry("One piece", 1),
        listOf(CopyOnBookshelf(copy.id, leasBookshelf.id, "Bibliothèque de Léa")),
      ),
    )
  }

  @Test
  fun `an edition on two of the reader's bookshelves is one book with both copies`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    val salon = bookshelfOf("Salon", Membership(sam, OWNER), Membership(lea, VIEWER))
    val leasCopy = copyOn(leasBookshelf, ROMANCE_DAWN.id).also { copies.insert(it) }
    val salonsCopy = copyOn(salon, ROMANCE_DAWN.id).also { copies.insert(it) }

    // When
    val result = findCatalogueSeries(lea, ONE_PIECE.id)

    // Then
    result.shouldBeInstanceOf<Found>().books.single().copies shouldContainExactlyInAnyOrder listOf(
      CopyOnBookshelf(leasCopy.id, leasBookshelf.id, "Bibliothèque de Léa"),
      CopyOnBookshelf(salonsCopy.id, salon.id, "Salon"),
    )
  }

  @Test
  fun `a copy on a bookshelf the reader does not belong to is left out`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    val samsBookshelf = bookshelfOf("Bibliothèque de Sam", Membership(sam, OWNER))
    val leasCopy = copyOn(leasBookshelf, ROMANCE_DAWN.id).also { copies.insert(it) }
    copies.insert(copyOn(samsBookshelf, ROMANCE_DAWN.id))

    // When
    val result = findCatalogueSeries(lea, ONE_PIECE.id)

    // Then
    result.shouldBeInstanceOf<Found>().books.single().copies shouldBe listOf(
      CopyOnBookshelf(leasCopy.id, leasBookshelf.id, "Bibliothèque de Léa"),
    )
  }

  @Test
  fun `a series only on bookshelves the reader does not belong to is no such series`() {
    // Given
    val samsBookshelf = bookshelfOf("Bibliothèque de Sam", Membership(sam, OWNER))
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(samsBookshelf, ROMANCE_DAWN.id))
    copies.insert(copyOn(leasBookshelf, ASTERIX_LE_GAULOIS.id))

    // When
    val result = findCatalogueSeries(lea, ONE_PIECE.id)

    // Then
    result shouldBe NoSuchSeries
  }

  private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
    bookshelfOf(BookshelfId.new(), BookshelfName(name), memberships.toList()).also { bookshelves.insert(it) }
}

private val ONE_PIECE = Series(SeriesId.new(), "One piece")

private val ASTERIX = Series(SeriesId.new(), "Astérix")

private val ROMANCE_DAWN_COVER = CoverName("3c5e7a9d0f2b4d6e8a1c3f5b7d9e0a2c4f6b8d1e3a5c7f9b0d2e4a6c8f1b3d5e")

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
  coverName = ROMANCE_DAWN_COVER,
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
