package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.application.RemoveCopyResult.NoSuchCopy
import fr.amory.libris.library.application.RemoveCopyResult.Removed
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

class RemoveCopyTest {
  private val copies = CopiesInMemory()
  private val bookshelves = BookshelvesInMemory()
  private val removeCopy = RemoveCopy(copies, bookshelves)

  @Test
  fun `an owner's copy is removed, the other copy of the edition kept`() {
    // Given
    val salon = salonOf(Membership(LEA, OWNER))
    val removed = copyOn(salon, ASTERIX_LE_GAULOIS)
    val kept = copyOn(salon, ASTERIX_LE_GAULOIS)
    copies.insert(removed)
    copies.insert(kept)

    // When
    val result = removeCopy(LEA, removed.id)

    // Then
    result shouldBe Removed
    copies.stored shouldBe listOf(kept)
  }

  @Test
  fun `a copy on a bookshelf the reader joins as a guest stays`() {
    // Given
    val salon = salonOf(Membership(SAM, OWNER), Membership(LEA, VIEWER))
    val copy = copyOn(salon, ASTERIX_LE_GAULOIS)
    copies.insert(copy)

    // When
    val result = removeCopy(LEA, copy.id)

    // Then
    result shouldBe NoSuchCopy
    copies.stored shouldBe listOf(copy)
  }

  private fun salonOf(vararg memberships: Membership): Bookshelf =
    bookshelfOf(BookshelfId.new(), BookshelfName("Salon"), memberships.toList()).also { bookshelves.insert(it) }
}

private val LEA = ReaderId.new()

private val ASTERIX_LE_GAULOIS = EditionId.new()

private val SAM = ReaderId.new()
