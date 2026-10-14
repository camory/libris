package fr.amory.libris.library.domain.bookshelf

import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.bookshelfOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class BookshelfTest {
  @Test
  fun `the bookshelf of a reader is named after them, owned and created by them`() {
    // Given
    val lea = ReaderId.new()
    val id = BookshelfId.new()
    val at = Instant.parse("2026-10-14T09:30:00Z")

    // When
    val bookshelf = Bookshelf.ownedBy(lea, "Léa", id, at)

    // Then
    bookshelf shouldBe Bookshelf(id, BookshelfName("Bibliothèque de Léa"), listOf(Membership(lea, OWNER)), lea, at)
  }

  @Test
  fun `a bookshelf tells its owners from its viewers and from strangers`() {
    // Given
    val lea = ReaderId.new()
    val juliette = ReaderId.new()
    val stranger = ReaderId.new()
    val bookshelf = bookshelfOf(
      BookshelfId.new(),
      BookshelfName("Salon"),
      listOf(Membership(lea, OWNER), Membership(juliette, VIEWER)),
    )

    // When / Then
    bookshelf.isOwnedBy(lea) shouldBe true
    bookshelf.isOwnedBy(juliette) shouldBe false
    bookshelf.isOwnedBy(stranger) shouldBe false
    bookshelf.hasMember(lea) shouldBe true
    bookshelf.hasMember(juliette) shouldBe true
    bookshelf.hasMember(stranger) shouldBe false
  }

  @Test
  fun `a bookshelf answers the role of each of its members`() {
    // Given
    val lea = ReaderId.new()
    val juliette = ReaderId.new()
    val bookshelf = bookshelfOf(
      BookshelfId.new(),
      BookshelfName("Salon"),
      listOf(Membership(lea, OWNER), Membership(juliette, VIEWER)),
    )

    // When / Then
    bookshelf.roleOf(lea) shouldBe OWNER
    bookshelf.roleOf(juliette) shouldBe VIEWER
  }

  @Test
  fun `a bookshelf answers no role for a reader who is no member`() {
    // Given
    val bookshelf = Bookshelf.ownedBy(ReaderId.new(), "Léa", BookshelfId.new(), Instant.parse("2026-10-14T09:30:00Z"))

    // When / Then
    bookshelf.roleOf(ReaderId.new()) shouldBe null
  }

  @Test
  fun `a member added joins the members a bookshelf had, its id and name kept`() {
    // Given
    val marc = ReaderId.new()
    val lea = ReaderId.new()
    val id = BookshelfId.new()
    val bookshelf = bookshelfOf(id, BookshelfName("Bibliothèque familiale"), listOf(Membership(marc, OWNER)))

    // When
    val added = bookshelf.withMember(Membership(lea, VIEWER))

    // Then
    added shouldBe bookshelfOf(
      id,
      BookshelfName("Bibliothèque familiale"),
      listOf(Membership(marc, OWNER), Membership(lea, VIEWER)),
    )
  }

  @Test
  fun `a bookshelf without an owner is refused`() {
    shouldThrow<IllegalArgumentException> {
      bookshelfOf(
        BookshelfId.new(),
        BookshelfName("Bibliothèque de personne"),
        listOf(Membership(ReaderId.new(), VIEWER)),
      )
    }
  }

  @Test
  fun `a bookshelf with a reader twice is refused, whatever the roles`() {
    val lea = ReaderId.new()
    shouldThrow<IllegalArgumentException> {
      bookshelfOf(
        BookshelfId.new(),
        BookshelfName("Bibliothèque familiale"),
        listOf(Membership(lea, OWNER), Membership(lea, VIEWER)),
      )
    }
  }
}
