package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import fr.amory.libris.library.application.catalogue.FindCatalogueEditionResult.Found
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
import org.junit.jupiter.api.Test

class FindCatalogueEditionTest {
  private val lea = ReaderId.new()
  private val bookshelves = BookshelvesInMemory()
  private val copies = CopiesInMemory()
  private val editions = EditionsInMemory().also {
    it.insert(ASTERIX_LE_GAULOIS)
    it.insert(ROMANCE_DAWN)
  }
  private val seriesRepository = SeriesInMemory().also { repository ->
    listOf(ASTERIX, ONE_PIECE).forEach { repository.findOrInsert(it) }
  }
  private val findCatalogueEdition = FindCatalogueEdition(bookshelves, copies, editions, seriesRepository)

  @Test
  fun `an edition of the reader's catalogue is found with its series and its copies`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, ROMANCE_DAWN.id))
    val copy = copyOn(leasBookshelf, ASTERIX_LE_GAULOIS.id).also { copies.insert(it) }

    // When
    val result = findCatalogueEdition(lea, ASTERIX_LE_GAULOIS.id)

    // Then
    result shouldBe Found(
      HeldEdition(
        ASTERIX_LE_GAULOIS,
        SeriesEntry("Astérix", 1),
        listOf(CopyOnBookshelf(copy.id, leasBookshelf.id, "Bibliothèque de Léa")),
      ),
    )
  }

  private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
    bookshelfOf(BookshelfId.new(), BookshelfName(name), memberships.toList()).also { bookshelves.insert(it) }
}

private val ASTERIX = Series(SeriesId.new(), "Astérix")

private val ONE_PIECE = Series(SeriesId.new(), "One piece")

private val ASTERIX_LE_GAULOIS = Edition(
  id = EditionId.new(),
  isbn = null,
  kind = BD,
  title = "Astérix le Gaulois",
  subtitle = null,
  contributions = Contributions.of(emptyList()),
  seriesVolume = SeriesVolume(ASTERIX.id, 1),
  collection = null,
  publisher = null,
  publicationYear = null,
  language = null,
  pageCount = null,
  summary = null,
  coverName = null,
)

private val ROMANCE_DAWN = ASTERIX_LE_GAULOIS.copy(
  id = EditionId.new(),
  kind = MANGA,
  title = "Romance dawn",
  seriesVolume = SeriesVolume(ONE_PIECE.id, 1),
)
