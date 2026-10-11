package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@JdbcSliceTest
@Import(JdbcReaderRepository::class, JdbcBookshelfRepository::class)
class JdbcReaderRepositoryTest @Autowired constructor(
  private val readers: JdbcReaderRepository,
  private val bookshelves: JdbcBookshelfRepository) {
  @Test
  fun `an inserted reader is read back whole`() {
    // Given
    val juliette = readerNamed("juliette", "Juliette")

    // When
    readers.insert(juliette)
    bookshelves.insert(bookshelfOwnedBy(juliette))

    // Then
    readers.findByUsername("juliette") shouldBe juliette
  }

  @Test
  fun `a reader is found whole by their id`() {
    // Given
    val juliette = readerNamed("juliette", "Juliette")
    readers.insert(juliette)

    // When
    val found = readers.findById(juliette.id)

    // Then
    found shouldBe juliette
  }

  @Test
  fun `an unknown id finds no reader`() {
    readers.findById(ReaderId.new()) shouldBe null
  }

  @Test
  fun `every reader stored is found`() {
    // Given
    val juliette = readerNamed("juliette", "Juliette")
    val lea = readerNamed("lea", "Léa")
    readers.insert(juliette)
    readers.insert(lea)

    // When
    val found = readers.findAll()

    // Then
    found shouldContainExactlyInAnyOrder listOf(juliette, lea)
  }

  @Test
  fun `an unknown username finds no reader`() {
    readers.findByUsername("nobody") shouldBe null
  }

  @Test
  fun `a second reader with the same username is refused`() {
    // Given
    val juliette = readerNamed("juliette", "Juliette")
    readers.insert(juliette)
    bookshelves.insert(bookshelfOwnedBy(juliette))
    val juju = readerNamed("juliette", "Juju", email = "juju@amory.fr")

    // When, Then
    shouldThrow<DuplicateUsernameException> { readers.insert(juju) }
  }
}
