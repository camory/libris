package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.cover.CoverName
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
    it.insert(LASTMAN_1)
    it.insert(LASTMAN_2)
    it.insert(THORGAL_1)
    it.insert(THORGAL_2)
    it.insert(BLAKE_ET_MORTIMER_1)
    it.insert(BLAKE_ET_MORTIMER_2)
    it.insert(LE_JOURNAL_D_UN_INGENU)
    it.insert(SPIROU_ET_FANTASIO_1)
    it.insert(LES_MURAILLES_DE_SAMARIS)
    it.insert(LA_FIEVRE_D_URBICANDE)
    it.insert(L_OMBRE_D_UN_HOMME)
    it.insert(L_ECHO_DES_CITES)
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

  @Test
  fun `a series mixing a BD and a manga has no kind`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, LASTMAN_1.id))
    copies.insert(copyOn(leasBookshelf, LASTMAN_2.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries("Lastman", 2, null, null))
  }

  @Test
  fun `a series has the cover of its first tome`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, THORGAL_1.id))
    copies.insert(copyOn(leasBookshelf, THORGAL_2.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries("Thorgal", 2, THORGAL_1_COVER, BD))
  }

  @Test
  fun `a series whose first tome has no cover has none, though the second has one`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, BLAKE_ET_MORTIMER_1.id))
    copies.insert(copyOn(leasBookshelf, BLAKE_ET_MORTIMER_2.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries("Blake et Mortimer", 2, null, BD))
  }

  @Test
  fun `an edition without a tome comes after the numbered tomes`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, LE_JOURNAL_D_UN_INGENU.id))
    copies.insert(copyOn(leasBookshelf, SPIROU_ET_FANTASIO_1.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries("Spirou et Fantasio", 2, SPIROU_ET_FANTASIO_1_COVER, BD))
  }

  @Test
  fun `a series without tomes has the cover of its first edition by title`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, LES_MURAILLES_DE_SAMARIS.id))
    copies.insert(copyOn(leasBookshelf, LA_FIEVRE_D_URBICANDE.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries("Les Cités obscures", 2, LA_FIEVRE_D_URBICANDE_COVER, BD))
  }

  @Test
  fun `titles are compared ignoring case and accents`() {
    // Given
    val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
    copies.insert(copyOn(leasBookshelf, L_OMBRE_D_UN_HOMME.id))
    copies.insert(copyOn(leasBookshelf, L_ECHO_DES_CITES.id))

    // When
    val series = listCatalogueSeries(lea)

    // Then
    series shouldBe listOf(CatalogueSeries("Les Cités obscures", 2, L_ECHO_DES_CITES_COVER, BD))
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

private val LASTMAN_1 = ROMANCE_DAWN.copy(
  id = EditionId.new(),
  kind = BD,
  title = "Lastman 1",
  series = SeriesEntry("Lastman", 1),
)

private val LASTMAN_2 = LASTMAN_1.copy(
  id = EditionId.new(),
  kind = MANGA,
  title = "Lastman 2",
  series = SeriesEntry("Lastman", 2),
)

private val THORGAL_1_COVER = CoverName("1a".repeat(32))

private val THORGAL_1 = ROMANCE_DAWN.copy(
  id = EditionId.new(),
  kind = BD,
  title = "La magicienne trahie",
  series = SeriesEntry("Thorgal", 1),
  coverName = THORGAL_1_COVER,
)

private val THORGAL_2 = THORGAL_1.copy(
  id = EditionId.new(),
  title = "L'île des mers gelées",
  series = SeriesEntry("Thorgal", 2),
  coverName = null,
)

private val BLAKE_ET_MORTIMER_1 = THORGAL_1.copy(
  id = EditionId.new(),
  title = "Le secret de l'Espadon",
  series = SeriesEntry("Blake et Mortimer", 1),
  coverName = null,
)

private val BLAKE_ET_MORTIMER_2_COVER = CoverName("2b".repeat(32))

private val BLAKE_ET_MORTIMER_2 = BLAKE_ET_MORTIMER_1.copy(
  id = EditionId.new(),
  title = "Le mystère de la Grande Pyramide",
  series = SeriesEntry("Blake et Mortimer", 2),
  coverName = BLAKE_ET_MORTIMER_2_COVER,
)

private val LE_JOURNAL_D_UN_INGENU_COVER = CoverName("3c".repeat(32))

private val LE_JOURNAL_D_UN_INGENU = THORGAL_1.copy(
  id = EditionId.new(),
  title = "Le journal d'un ingénu",
  series = SeriesEntry("Spirou et Fantasio", null),
  coverName = LE_JOURNAL_D_UN_INGENU_COVER,
)

private val SPIROU_ET_FANTASIO_1_COVER = CoverName("4d".repeat(32))

private val SPIROU_ET_FANTASIO_1 = LE_JOURNAL_D_UN_INGENU.copy(
  id = EditionId.new(),
  title = "4 aventures de Spirou et Fantasio",
  series = SeriesEntry("Spirou et Fantasio", 1),
  coverName = SPIROU_ET_FANTASIO_1_COVER,
)

private val LES_MURAILLES_DE_SAMARIS_COVER = CoverName("5e".repeat(32))

private val LES_MURAILLES_DE_SAMARIS = THORGAL_1.copy(
  id = EditionId.new(),
  title = "Les murailles de Samaris",
  series = SeriesEntry("Les Cités obscures", null),
  coverName = LES_MURAILLES_DE_SAMARIS_COVER,
)

private val LA_FIEVRE_D_URBICANDE_COVER = CoverName("6f".repeat(32))

private val LA_FIEVRE_D_URBICANDE = LES_MURAILLES_DE_SAMARIS.copy(
  id = EditionId.new(),
  title = "La fièvre d'Urbicande",
  coverName = LA_FIEVRE_D_URBICANDE_COVER,
)

private val L_OMBRE_D_UN_HOMME_COVER = CoverName("7a".repeat(32))

private val L_OMBRE_D_UN_HOMME = LES_MURAILLES_DE_SAMARIS.copy(
  id = EditionId.new(),
  title = "L'Ombre d'un homme",
  coverName = L_OMBRE_D_UN_HOMME_COVER,
)

private val L_ECHO_DES_CITES_COVER = CoverName("8b".repeat(32))

private val L_ECHO_DES_CITES = LES_MURAILLES_DE_SAMARIS.copy(
  id = EditionId.new(),
  title = "L'écho des cités",
  coverName = L_ECHO_DES_CITES_COVER,
)
