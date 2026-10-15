package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CopiesInMemory
import fr.amory.libris.library.fixture.bookshelfOf
import fr.amory.libris.library.fixture.copyOn
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
  }
  private val listCatalogueSeries = ListCatalogueSeries(bookshelves, copies, editions)

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
    series shouldBe listOf(CatalogueSeries("One piece", 2, null, MANGA))
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
    series shouldBe listOf(CatalogueSeries("Astérix", 2, null, BD))
  }

  private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
    bookshelfOf(BookshelfId.new(), BookshelfName(name), memberships.toList()).also { bookshelves.insert(it) }
}

private val ROMANCE_DAWN = Edition(
  id = EditionId.new(),
  isbn = null,
  kind = MANGA,
  title = "Romance dawn",
  subtitle = null,
  contributions = Contributions.of(emptyList()),
  series = SeriesEntry("One piece", 1),
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
  series = SeriesEntry("One piece", 2),
)

private val ASTERIX_LE_GAULOIS = ROMANCE_DAWN.copy(
  id = EditionId.new(),
  kind = BD,
  title = "Astérix le Gaulois",
  series = SeriesEntry("Astérix", 1),
)

private val LA_SERPE_D_OR = ASTERIX_LE_GAULOIS.copy(
  id = EditionId.new(),
  title = "La serpe d'or",
  series = SeriesEntry("Astérix", 2),
)
