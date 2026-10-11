package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException

@JdbcSliceTest
@Import(JdbcBookshelfRepository::class, JdbcReaderRepository::class)
class JdbcBookshelfRepositoryTest @Autowired constructor(
  private val bookshelves: JdbcBookshelfRepository,
  private val readers: JdbcReaderRepository) {
  @Test
  fun `an inserted bookshelf is read back whole, its members included`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val juliette = readerNamed("juliette", "Juliette")
    val bookshelf = Bookshelf(
      id = lea.defaultBookshelfId,
      name = BookshelfName("Bibliothèque de Léa"),
      memberships = listOf(Membership(lea.id, OWNER), Membership(juliette.id, VIEWER)),
    )

    readers.insert(lea)
    readers.insert(juliette)
    bookshelves.insert(bookshelfOwnedBy(juliette))

    // When
    bookshelves.insert(bookshelf)

    // Then
    val read = requireNotNull(bookshelves.findById(bookshelf.id))
    read.memberships.toSet() shouldBe bookshelf.memberships.toSet()
    read.copy(memberships = bookshelf.memberships) shouldBe bookshelf
  }

  @Test
  fun `an updated bookshelf is read back whole, as updated`() {
    // Given
    val lea = readerNamed("lea", "Léa").also { readers.insert(it) }
    val juliette = readerNamed("juliette", "Juliette").also { readers.insert(it) }
    val bookshelf = bookshelfOwnedBy(lea).also { bookshelves.insert(it) }
    val updated = Bookshelf(
      id = bookshelf.id,
      name = BookshelfName("Salon"),
      memberships = listOf(Membership(lea.id, OWNER), Membership(juliette.id, VIEWER)),
    )

    // When
    bookshelves.update(updated)

    // Then
    val read = requireNotNull(bookshelves.findById(bookshelf.id))
    read.memberships.toSet() shouldBe updated.memberships.toSet()
    read.copy(memberships = updated.memberships) shouldBe updated
  }

  @Test
  fun `an update leaves another bookshelf's members as they were`() {
    // Given
    val lea = readerNamed("lea", "Léa").also { readers.insert(it) }
    val tom = readerNamed("tom", "Tom").also { readers.insert(it) }
    val leasBookshelf = bookshelfOwnedBy(lea).also { bookshelves.insert(it) }
    val tomsBookshelf = bookshelfOwnedBy(tom).also { bookshelves.insert(it) }

    // When
    bookshelves.update(leasBookshelf.withMember(Membership(tom.id, VIEWER)))

    // Then
    bookshelves.findById(tomsBookshelf.id) shouldBe tomsBookshelf
  }

  @Test
  fun `a membership of a reader Libris does not know is refused`() {
    val nobody = readerNamed("nobody", "Nobody")

    shouldThrow<DataIntegrityViolationException> { bookshelves.insert(bookshelfOwnedBy(nobody)) }
  }

  @Test
  fun `the bookshelves a reader is a member of are found whole, none of another's`() {
    // Given
    val lea = readerNamed("lea", "Léa").also { readers.insert(it) }
    val tom = readerNamed("tom", "Tom").also { readers.insert(it) }
    val leasBookshelf = bookshelfOwnedBy(lea).also { bookshelves.insert(it) }
    bookshelfOwnedBy(tom).also { bookshelves.insert(it) }
    val salon = Bookshelf(
      BookshelfId.new(),
      BookshelfName("Salon"),
      listOf(Membership(tom.id, OWNER), Membership(lea.id, VIEWER)),
    ).also { bookshelves.insert(it) }

    // When
    val found = bookshelves.findByMember(lea.id)

    // Then
    found.map { Triple(it.id, it.name, it.memberships.toSet()) } shouldContainExactlyInAnyOrder listOf(
      Triple(leasBookshelf.id, BookshelfName("Bibliothèque de Léa"), setOf(Membership(lea.id, OWNER))),
      Triple(salon.id, BookshelfName("Salon"), setOf(Membership(tom.id, OWNER), Membership(lea.id, VIEWER))),
    )
  }

  @Test
  fun `an unknown id finds no bookshelf`() {
    bookshelves.findById(BookshelfId.new()) shouldBe null
  }
}
