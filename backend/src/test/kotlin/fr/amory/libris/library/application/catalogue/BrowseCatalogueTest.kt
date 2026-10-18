package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.EditionIdPage
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CatalogueEditionsAnswering
import fr.amory.libris.library.fixture.CopiesInMemory
import fr.amory.libris.library.fixture.bookshelfOf
import fr.amory.libris.library.fixture.copyOn
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BrowseCatalogueTest {
  private val lea = ReaderId.new()
  private val tom = ReaderId.new()
  private val bookshelves = BookshelvesInMemory()
  private val copies = CopiesInMemory()
  private val editions = EditionsInMemory().also {
    it.insert(CIGARES)
    it.insert(PIERRE)
  }
  private val seriesRepository = SeriesInMemory().also { it.findOrInsert(TINTIN) }

  @Test
  fun `the catalogue answers the port's editions in its order, with the reader's copies and its next`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    val salon = bookshelfOf("Salon", Membership(tom, OWNER), Membership(lea, VIEWER))
    val pierreOnLeasBookshelf = copyOf(PIERRE, leasBookshelf)
    val cigaresOnLeasBookshelf = copyOf(CIGARES, leasBookshelf)
    val cigaresInTheSalon = copyOf(CIGARES, salon)
    val next = EditionId.new()
    val browseCatalogue = browsing(EditionIdPage(listOf(CIGARES.id, PIERRE.id), next))

    // When
    val page = browseCatalogue(lea, null)

    // Then
    page shouldBe CataloguePage(
      listOf(
        HeldEdition(
          CIGARES,
          SeriesEntry("Tintin", 1),
          listOf(
            CopyOnBookshelf(cigaresOnLeasBookshelf.id, leasBookshelf.id, "Bibliothèque de Léa"),
            CopyOnBookshelf(cigaresInTheSalon.id, salon.id, "Salon"),
          ),
        ),
        HeldEdition(
          PIERRE,
          null,
          listOf(CopyOnBookshelf(pierreOnLeasBookshelf.id, leasBookshelf.id, "Bibliothèque de Léa")),
        ),
      ),
      next,
    )
  }

  @Test
  fun `the catalogue holds the reader's copies, none of another's`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    val tomsBookshelf = bookshelfOf("Bibliothèque de Tom", Membership(tom, OWNER))
    val leasCopy = copyOf(CIGARES, leasBookshelf)
    copyOf(CIGARES, tomsBookshelf)
    val browseCatalogue = browsing(EditionIdPage(listOf(CIGARES.id), null))

    // When
    val page = browseCatalogue(lea, null)

    // Then
    page shouldBe CataloguePage(
      listOf(
        HeldEdition(
          CIGARES,
          SeriesEntry("Tintin", 1),
          listOf(CopyOnBookshelf(leasCopy.id, leasBookshelf.id, "Bibliothèque de Léa")),
        ),
      ),
      null,
    )
  }

  @Test
  fun `the catalogue asks the port for fifty editions of the reader after the one given`() {
    // Given
    val after = EditionId.new()
    val catalogueEditions = CatalogueEditionsAnswering(EditionIdPage(emptyList(), null))
    val browseCatalogue = BrowseCatalogue(catalogueEditions, bookshelves, copies, editions, seriesRepository)

    // When
    browseCatalogue(lea, after)

    // Then
    catalogueEditions.asked shouldBe listOf(Triple(lea, after, 50))
  }

  private fun browsing(answer: EditionIdPage): BrowseCatalogue =
    BrowseCatalogue(CatalogueEditionsAnswering(answer), bookshelves, copies, editions, seriesRepository)

  private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
    bookshelfOf(BookshelfId.new(), BookshelfName(name), memberships.toList()).also { bookshelves.insert(it) }

  private fun copyOf(edition: Edition, bookshelf: Bookshelf): Copy =
    copyOn(bookshelf, edition.id).also { copies.insert(it) }
}

private val TINTIN = Series(SeriesId.new(), "Tintin")

private val CIGARES = Edition(
  id = EditionId.new(),
  isbn = null,
  kind = BD,
  title = "Les cigares du pharaon",
  subtitle = null,
  contributions = Contributions.of(emptyList()),
  seriesVolume = SeriesVolume(TINTIN.id, 1),
  collection = null,
  publisher = null,
  publicationYear = null,
  language = null,
  pageCount = null,
  summary = null,
  coverName = null,
)

private val PIERRE = CIGARES.copy(
  id = EditionId.new(),
  kind = BOOK,
  title = "Pierre et le loup",
  seriesVolume = null,
)
