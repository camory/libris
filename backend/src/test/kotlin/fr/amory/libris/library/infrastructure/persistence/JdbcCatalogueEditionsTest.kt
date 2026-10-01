package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcEditionRepository
import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.copy.EditionIdPage
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.UUID

private const val PAGE_SIZE = 50
private const val LESSER_TWIN = "00000000-0000-7000-8000-000000000001"
private const val GREATER_TWIN = "00000000-0000-7000-8000-000000000002"

@JdbcSliceTest
@Import(
  JdbcCatalogueEditions::class,
  JdbcCopyRepository::class,
  JdbcEditionRepository::class,
  JdbcBookshelfRepository::class,
  JdbcReaderRepository::class,
)
class JdbcCatalogueEditionsTest @Autowired constructor(
  private val catalogueEditions: JdbcCatalogueEditions,
  private val copies: JdbcCopyRepository,
  private val editions: JdbcEditionRepository,
  private val bookshelves: JdbcBookshelfRepository,
  private val readers: JdbcReaderRepository,
) {
  private val lea = readerNamed("lea", "Léa")
  private val tom = readerNamed("tom", "Tom")
  private val leasBookshelf = bookshelfOwnedBy(lea)
  private val tomsBookshelf = bookshelfOwnedBy(tom)

  @BeforeEach
  fun insertLeaAndTom() {
    listOf(lea, tom).forEach(readers::insert)
    listOf(leasBookshelf, tomsBookshelf).forEach(bookshelves::insert)
  }

  @Test
  fun `the page holds the reader's editions alone`() {
    // Given
    val leas = heldOn(leasBookshelf, edition("Les cigares du pharaon", "Tintin", 1))
    heldOn(tomsBookshelf, edition("Pierre et le loup"))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    page shouldBe EditionIdPage(listOf(leas.id), null)
  }

  @Test
  fun `editions are ordered by series name, or by title without a series`() {
    // Given
    heldOn(leasBookshelf, edition("Les cigares du pharaon", "Tintin", 1))
    heldOn(leasBookshelf, edition("Pierre et le loup"))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    titlesOf(page) shouldBe listOf("Pierre et le loup", "Les cigares du pharaon")
  }

  @Test
  fun `the order ignores case`() {
    // Given
    heldOn(leasBookshelf, edition("Un ninja", "Naruto", 1))
    heldOn(leasBookshelf, edition("maus"))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    titlesOf(page) shouldBe listOf("maus", "Un ninja")
  }

  @Test
  fun `the order ignores accents`() {
    // Given
    heldOn(leasBookshelf, edition("Légendes en exil", "Fables", 1))
    heldOn(leasBookshelf, edition("Émile et les détectives"))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    titlesOf(page) shouldBe listOf("Émile et les détectives", "Légendes en exil")
  }

  @Test
  fun `the tomes of a series are ordered as numbers`() {
    // Given
    heldOn(leasBookshelf, edition("Le vrai visage d'Arlong", "One piece", 10))
    heldOn(leasBookshelf, edition("Une vérité qui blesse", "One piece", 3))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    titlesOf(page) shouldBe listOf("Une vérité qui blesse", "Le vrai visage d'Arlong")
  }

  @Test
  fun `an edition of the series without a tome comes after its numbered tomes`() {
    // Given
    heldOn(leasBookshelf, edition("Astérix et ses amis", "Astérix"))
    heldOn(leasBookshelf, edition("Astérix le Gaulois", "Astérix", 1))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    titlesOf(page) shouldBe listOf("Astérix le Gaulois", "Astérix et ses amis")
  }

  @Test
  fun `editions of one series and tome are ordered by title`() {
    // Given
    heldOn(leasBookshelf, edition("Romance dawn", "One piece", 1))
    heldOn(leasBookshelf, edition("À l'aube d'une grande aventure", "One piece", 1))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    titlesOf(page) shouldBe listOf("À l'aube d'une grande aventure", "Romance dawn")
  }

  @Test
  fun `editions equal on series, tome and title are ordered by id`() {
    // Given
    val first = EditionId(UUID.fromString(LESSER_TWIN))
    val second = EditionId(UUID.fromString(GREATER_TWIN))
    heldOn(leasBookshelf, edition("Romance dawn", "One piece", 1, second))
    heldOn(leasBookshelf, edition("Romance dawn", "One piece", 1, first))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    page.editionIds shouldBe listOf(first, second)
  }

  @Test
  fun `a full page names its last edition as next`() {
    // Given
    val romans = romansHeldOn(leasBookshelf, 1..3)

    // When
    val page = catalogueEditions.findPage(lea.id, null, 2)

    // Then
    titlesOf(page) shouldBe listOf("Roman 01", "Roman 02")
    page.next shouldBe romans.getValue(2).id
  }

  @Test
  fun `the page after an edition starts just after it`() {
    // Given
    val romans = romansHeldOn(leasBookshelf, 1..3)

    // When
    val page = catalogueEditions.findPage(lea.id, romans.getValue(2).id, 2)

    // Then
    titlesOf(page) shouldBe listOf("Roman 03")
    page.next shouldBe null
  }

  @Test
  fun `a page that ends the catalogue names no next`() {
    // Given
    romansHeldOn(leasBookshelf, 1..2)

    // When
    val page = catalogueEditions.findPage(lea.id, null, 2)

    // Then
    titlesOf(page) shouldBe listOf("Roman 01", "Roman 02")
    page.next shouldBe null
  }

  @Test
  fun `the pages after the tomes of a series reach its edition without a tome, then go on`() {
    // Given
    heldOn(leasBookshelf, edition("Babar"))
    val withoutTome = heldOn(leasBookshelf, edition("Astérix et ses amis", "Astérix"))
    val tomeTwo = heldOn(leasBookshelf, edition("La serpe d'or", "Astérix", 2))
    val tomeOne = heldOn(leasBookshelf, edition("Astérix le Gaulois", "Astérix", 1))

    // When
    val afterTomeOne = catalogueEditions.findPage(lea.id, tomeOne.id, 1)
    val afterTomeTwo = catalogueEditions.findPage(lea.id, tomeTwo.id, 1)
    val afterWithoutTome = catalogueEditions.findPage(lea.id, withoutTome.id, 1)

    // Then
    titlesOf(afterTomeOne) shouldBe listOf("La serpe d'or")
    titlesOf(afterTomeTwo) shouldBe listOf("Astérix et ses amis")
    titlesOf(afterWithoutTome) shouldBe listOf("Babar")
  }

  @Test
  fun `an after the reader no longer holds continues after its place`() {
    // Given
    heldOn(leasBookshelf, roman(1))
    val roman02 = heldOn(tomsBookshelf, roman(2))
    heldOn(leasBookshelf, roman(3))
    heldOn(leasBookshelf, roman(4))

    // When
    val page = catalogueEditions.findPage(lea.id, roman02.id, 2)

    // Then
    titlesOf(page) shouldBe listOf("Roman 03", "Roman 04")
    page.next shouldBe null
  }

  @Test
  fun `an after naming no edition answers an empty page`() {
    // Given
    heldOn(leasBookshelf, roman(1))

    // When
    val page = catalogueEditions.findPage(lea.id, EditionId.new(), 2)

    // Then
    page shouldBe EditionIdPage(emptyList(), null)
  }

  @Test
  fun `editions equal but for their id are split across a page without a repeat or a gap`() {
    // Given
    val roman01 = heldOn(leasBookshelf, roman(1))
    val greaterTwin = heldOn(leasBookshelf, roman(2, EditionId(UUID.fromString(GREATER_TWIN))))
    val lesserTwin = heldOn(leasBookshelf, roman(2, EditionId(UUID.fromString(LESSER_TWIN))))

    // When
    val first = catalogueEditions.findPage(lea.id, null, 2)
    val second = catalogueEditions.findPage(lea.id, first.next, 2)

    // Then
    first shouldBe EditionIdPage(listOf(roman01.id, lesserTwin.id), lesserTwin.id)
    second shouldBe EditionIdPage(listOf(greaterTwin.id), null)
  }

  @Test
  fun `an edition added between two pages does not shift the next`() {
    // Given
    romansHeldOn(leasBookshelf, 1..3)
    val first = catalogueEditions.findPage(lea.id, null, 2)
    heldOn(leasBookshelf, roman(0))

    // When
    val second = catalogueEditions.findPage(lea.id, first.next, 2)

    // Then
    titlesOf(second) shouldBe listOf("Roman 03")
    second.next shouldBe null
  }

  @Test
  fun `an edition on two of the reader's bookshelves comes once`() {
    // Given
    val salon = Bookshelf(BookshelfId.new(), "Salon", listOf(Membership(lea.id, OWNER)))
    bookshelves.insert(salon)
    val edition = heldOn(leasBookshelf, heldOn(salon, roman(1)))

    // When
    val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

    // Then
    page shouldBe EditionIdPage(listOf(edition.id), null)
  }

  private fun titlesOf(page: EditionIdPage): List<String> {
    val titles = editions.findByIds(page.editionIds).associate { it.id to it.title }
    return page.editionIds.map { titles.getValue(it) }
  }

  private fun edition(
    title: String,
    seriesName: String? = null,
    volumeNumber: Int? = null,
    id: EditionId = EditionId.new(),
  ): Edition = Edition(
    id = id,
    isbn = null,
    kind = BD,
    title = title,
    subtitle = null,
    contributions = Contributions.of(emptyList()),
    series = seriesName?.let { SeriesEntry(it, volumeNumber) },
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverUrl = null,
  ).also(editions::insert)

  private fun romansHeldOn(bookshelf: Bookshelf, numbers: IntRange): Map<Int, Edition> =
    numbers.reversed().associateWith { heldOn(bookshelf, roman(it)) }

  private fun roman(number: Int, id: EditionId = EditionId.new()): Edition =
    edition("Roman %02d".format(number), id = id)

  private fun heldOn(bookshelf: Bookshelf, edition: Edition): Edition {
    copies.insert(Copy(CopyId.new(), edition.id, bookshelf.id))
    return edition
  }
}
