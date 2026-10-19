package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.SeriesInMemory
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
import org.junit.jupiter.api.Test

class ListCatalogueSeriesTest {
  private val lea = ReaderId.new()
  private val sam = ReaderId.new()
  private val bookshelves = BookshelvesInMemory()
  private val copies = CopiesInMemory()
  private val editions = EditionsInMemory().also {
    it.insert(ROMANCE_DAWN)
    it.insert(BAGGY)
    it.insert(ASTERIX_LE_GAULOIS)
    it.insert(LA_SERPE_D_OR)
    it.insert(LASTMAN_1)
    it.insert(LASTMAN_2)
    it.insert(BLACKSAD_1)
    it.insert(BLACKSAD_2)
    it.insert(NARUTO_1)
    it.insert(NARUTO_2)
  }
  private val seriesRepository = SeriesInMemory().also { repository ->
    listOf(ONE_PIECE, ASTERIX, LASTMAN, BLACKSAD, NARUTO).forEach { repository.findOrInsert(it) }
  }
  private val catalogueEditions = CatalogueEditionsAnswering(
    firstOfEachSeries = setOf(ROMANCE_DAWN.id, ASTERIX_LE_GAULOIS.id, LASTMAN_1.id, BLACKSAD_1.id, NARUTO_1.id),
  )
  private val listCatalogueSeries =
    ListCatalogueSeries(catalogueEditions, bookshelves, copies, editions, seriesRepository)

  @Test
  fun `a series counts its editions, each once however many copies it has`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    val salon = bookshelfOf("Salon", Membership(sam, OWNER), Membership(lea, VIEWER))
    copies.insert(copyOn(leasBookshelf, ROMANCE_DAWN.id))
    copies.insert(copyOn(salon, ROMANCE_DAWN.id))
    copies.insert(copyOn(leasBookshelf, BAGGY.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries(ONE_PIECE.id, "One piece", 2, null, MANGA))
  }

  @Test
  fun `a series whose editions are all BD has the kind BD`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, ASTERIX_LE_GAULOIS.id))
    copies.insert(copyOn(leasBookshelf, LA_SERPE_D_OR.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries(ASTERIX.id, "Astérix", 2, null, BD))
  }

  @Test
  fun `a series mixing a BD and a manga has no kind`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, LASTMAN_1.id))
    copies.insert(copyOn(leasBookshelf, LASTMAN_2.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries(LASTMAN.id, "Lastman", 2, null, null))
  }

  @Test
  fun `a series answers the cover of its first edition`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, BLACKSAD_1.id))
    copies.insert(copyOn(leasBookshelf, BLACKSAD_2.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries(BLACKSAD.id, "Blacksad", 2, BLACKSAD_1_COVER, BD))
  }

  @Test
  fun `a series whose first edition has no cover answers none`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, NARUTO_1.id))
    copies.insert(copyOn(leasBookshelf, NARUTO_2.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries(NARUTO.id, "Naruto", 2, null, MANGA))
  }

  @Test
  fun `each series answers the id of its series`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, ROMANCE_DAWN.id))
    copies.insert(copyOn(leasBookshelf, ASTERIX_LE_GAULOIS.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series.map { it.id to it.name } shouldContainExactlyInAnyOrder listOf(
      ONE_PIECE.id to "One piece",
      ASTERIX.id to "Astérix",
    )
  }

  private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
    bookshelfOf(BookshelfId.new(), BookshelfName(name), memberships.toList()).also { bookshelves.insert(it) }
}

private val ONE_PIECE = Series(SeriesId.new(), "One piece")

private val ASTERIX = Series(SeriesId.new(), "Astérix")

private val LASTMAN = Series(SeriesId.new(), "Lastman")

private val BLACKSAD = Series(SeriesId.new(), "Blacksad")

private val NARUTO = Series(SeriesId.new(), "Naruto")

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

private val LA_SERPE_D_OR = ASTERIX_LE_GAULOIS.copy(
  id = EditionId.new(),
  title = "La serpe d'or",
  seriesVolume = SeriesVolume(ASTERIX.id, 2),
)

private val LASTMAN_1 = ROMANCE_DAWN.copy(
  id = EditionId.new(),
  kind = BD,
  title = "Lastman 1",
  seriesVolume = SeriesVolume(LASTMAN.id, 1),
)

private val LASTMAN_2 = LASTMAN_1.copy(
  id = EditionId.new(),
  kind = MANGA,
  title = "Lastman 2",
  seriesVolume = SeriesVolume(LASTMAN.id, 2),
)

private val BLACKSAD_1_COVER = CoverName("6f2b0c4e8a1d3f5b7c9e0a2d4f6b8c1e3a5d7f9b0c2e4a6d8f1b3c5e7a9d0f2b")

private val BLACKSAD_1 = ASTERIX_LE_GAULOIS.copy(
  id = EditionId.new(),
  title = "Quelque part entre les ombres",
  seriesVolume = SeriesVolume(BLACKSAD.id, 1),
  coverName = BLACKSAD_1_COVER,
)

private val BLACKSAD_2 = BLACKSAD_1.copy(
  id = EditionId.new(),
  title = "Arctic-Nation",
  seriesVolume = SeriesVolume(BLACKSAD.id, 2),
  coverName = null,
)

private val NARUTO_2_COVER = CoverName("0d3e5a7c9f1b2d4e6a8c0f3b5d7e9a1c4f6b8d0e2a5c7f9b1d3e6a8c0f2b4d7e")

private val NARUTO_1 = ROMANCE_DAWN.copy(
  id = EditionId.new(),
  title = "Naruto Uzumaki",
  seriesVolume = SeriesVolume(NARUTO.id, 1),
)

private val NARUTO_2 = NARUTO_1.copy(
  id = EditionId.new(),
  title = "Un client embarrassant",
  seriesVolume = SeriesVolume(NARUTO.id, 2),
  coverName = NARUTO_2_COVER,
)
