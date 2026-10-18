package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcEditionRepository
import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.copyOn
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.simple.JdbcClient
import java.time.Instant
import java.util.UUID

private const val ONE_PIECE = "9782723488525"

@JdbcSliceTest
@Import(
  JdbcCopyRepository::class,
  JdbcEditionRepository::class,
  JdbcBookshelfRepository::class,
  JdbcReaderRepository::class,
)
class JdbcCopyRepositoryTest @Autowired constructor(
  private val copies: JdbcCopyRepository,
  private val editions: JdbcEditionRepository,
  private val bookshelves: JdbcBookshelfRepository,
  private val readers: JdbcReaderRepository,
  private val jdbcClient: JdbcClient) {
  @Test
  fun `a copy inserted sits on its bookshelf and its edition`() {
    // Given
    val bookshelf = bookshelfOf("lea", "Léa")
    val edition = onePieceTomeOne()
    editions.insert(edition)
    val copy = copyOn(bookshelf, edition.id)

    // When
    copies.insert(copy)

    // Then
    rowOf(copy.id) shouldBe (edition.id.value to bookshelf.id.value)
  }

  @Test
  fun `a copy inserted is read back whole, its adding included`() {
    // Given
    val bookshelf = bookshelfOf("lea", "Léa")
    val juliette = readerNamed("juliette", "Juliette").also { readers.insert(it) }
    val edition = onePieceTomeOne()
    editions.insert(edition)
    val copy = Copy(CopyId.new(), edition.id, bookshelf.id, juliette.id, Instant.parse("2026-10-14T09:30:00.123456Z"))

    // When
    copies.insert(copy)

    // Then
    copies.findByEditionId(edition.id) shouldBe listOf(copy)
  }

  @Test
  fun `a copy added by a reader Libris does not know is refused`() {
    // Given
    val bookshelf = bookshelfOf("lea", "Léa")
    val edition = onePieceTomeOne()
    editions.insert(edition)

    // When
    val copy = copyOn(bookshelf, edition.id).copy(addedBy = ReaderId.new())

    // Then
    shouldThrow<DataIntegrityViolationException> { copies.insert(copy) }
  }

  @Test
  fun `two copies of one edition on one bookshelf stand beside each other`() {
    // Given
    val bookshelf = bookshelfOf("lea", "Léa")
    val edition = onePieceTomeOne()
    editions.insert(edition)
    val first = copyOn(bookshelf, edition.id)
    copies.insert(first)

    // When
    val second = copyOn(bookshelf, edition.id)
    copies.insert(second)

    // Then
    rowOf(first.id) shouldBe (edition.id.value to bookshelf.id.value)
    rowOf(second.id) shouldBe (edition.id.value to bookshelf.id.value)
  }

  @Test
  fun `a copy of an edition the house does not hold is refused`() {
    // Given
    val bookshelf = bookshelfOf("lea", "Léa")

    // When
    val copy = copyOn(bookshelf, EditionId.new())

    // Then
    shouldThrow<DataIntegrityViolationException> { copies.insert(copy) }
  }

  @Test
  fun `a copy on a bookshelf Libris does not know is refused`() {
    // Given
    val lea = readerNamed("lea", "Léa").also { readers.insert(it) }
    val edition = onePieceTomeOne()
    editions.insert(edition)

    // When
    val copy = Copy(CopyId.new(), edition.id, BookshelfId.new(), lea.id, Instant.parse("2026-10-14T09:30:00Z"))

    // Then
    shouldThrow<DataIntegrityViolationException> { copies.insert(copy) }
  }

  @Test
  fun `the copies of an edition are found, wherever they sit`() {
    // Given
    val leasBookshelf = bookshelfOf("lea", "Léa")
    val juliettesBookshelf = bookshelfOf("juliette", "Juliette")
    val edition = onePieceTomeOne()
    editions.insert(edition)
    val leasCopy = copyOn(leasBookshelf, edition.id)
    val juliettesCopy = copyOn(juliettesBookshelf, edition.id)
    copies.insert(leasCopy)
    copies.insert(juliettesCopy)

    // When
    val found = copies.findByEditionId(edition.id)

    // Then
    found.toSet() shouldBe setOf(leasCopy, juliettesCopy)
  }

  @Test
  fun `the copies of another edition are not found`() {
    // Given
    val bookshelf = bookshelfOf("lea", "Léa")
    val edition = onePieceTomeOne()
    editions.insert(edition)
    val other = onePieceTomeOne().copy(id = EditionId.new(), isbn = isbnOf("9782723489898"))
    editions.insert(other)
    copies.insert(copyOn(bookshelf, other.id))

    // When
    val found = copies.findByEditionId(edition.id)

    // Then
    found shouldBe emptyList()
  }

  @Test
  fun `the copies of the given editions are found, wherever they sit, none of another`() {
    // Given
    val leasBookshelf = bookshelfOf("lea", "Léa")
    val tomsBookshelf = bookshelfOf("tom", "Tom")
    val romanceDawn = onePieceTomeOne()
    val baggy = onePieceTomeOne().copy(id = EditionId.new(), isbn = isbnOf("9782723489898"))
    val other = onePieceTomeOne().copy(id = EditionId.new(), isbn = isbnOf("9782723490030"))
    listOf(romanceDawn, baggy, other).forEach(editions::insert)
    val leasCopy = copyOn(leasBookshelf, romanceDawn.id)
    val tomsCopy = copyOn(tomsBookshelf, baggy.id)
    copies.insert(leasCopy)
    copies.insert(tomsCopy)
    copies.insert(copyOn(leasBookshelf, other.id))

    // When
    val found = copies.findByEditionIds(listOf(romanceDawn.id, baggy.id))

    // Then
    found.toSet() shouldBe setOf(leasCopy, tomsCopy)
  }

  @Test
  fun `no edition finds no copy`() {
    copies.findByEditionIds(emptyList()) shouldBe emptyList()
  }

  @Test
  fun `the copies on the given bookshelves are found, none on another`() {
    // Given
    val leasBookshelf = bookshelfOf("lea", "Léa")
    val tomsBookshelf = bookshelfOf("tom", "Tom")
    val samsBookshelf = bookshelfOf("sam", "Sam")
    val edition = onePieceTomeOne()
    editions.insert(edition)
    val leasCopy = copyOn(leasBookshelf, edition.id)
    val tomsCopy = copyOn(tomsBookshelf, edition.id)
    copies.insert(leasCopy)
    copies.insert(tomsCopy)
    copies.insert(copyOn(samsBookshelf, edition.id))

    // When
    val found = copies.findByBookshelfIds(listOf(leasBookshelf.id, tomsBookshelf.id))

    // Then
    found.toSet() shouldBe setOf(leasCopy, tomsCopy)
  }

  @Test
  fun `no bookshelf finds no copy`() {
    copies.findByBookshelfIds(emptyList()) shouldBe emptyList()
  }

  private fun bookshelfOf(username: String, displayName: String): Bookshelf {
    val reader = readerNamed(username, displayName)
    readers.insert(reader)
    val bookshelf = bookshelfOwnedBy(reader)
    bookshelves.insert(bookshelf)
    return bookshelf
  }

  private fun rowOf(id: CopyId): Pair<UUID, UUID>? =
    jdbcClient
      .sql("SELECT copy.edition_id, copy.bookshelf_id FROM copy WHERE copy.id = :id")
      .param("id", id.value)
      .query { rs, _ ->
        rs.getObject("edition_id", UUID::class.java) to rs.getObject("bookshelf_id", UUID::class.java)
      }
      .list()
      .singleOrNull()

  private fun onePieceTomeOne(): Edition =
    Edition(
      id = EditionId.new(),
      isbn = isbnOf(ONE_PIECE),
      kind = MANGA,
      title = "Romance dawn",
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
}
