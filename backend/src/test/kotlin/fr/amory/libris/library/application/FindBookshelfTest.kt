package fr.amory.libris.library.application

import fr.amory.libris.library.application.FindBookshelfResult.Found
import fr.amory.libris.library.application.FindBookshelfResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.ReadersInMemory
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class FindBookshelfTest {
  private val marc = readerNamed("marc", "Marc")
  private val lea = readerNamed("lea", "Léa")
  private val familiale = Bookshelf(
    BookshelfId.new(),
    BookshelfName("Bibliothèque familiale"),
    listOf(Membership(marc.id, OWNER), Membership(lea.id, VIEWER)),
  )
  private val bookshelves = BookshelvesInMemory().apply { insert(familiale) }
  private val readers = ReadersInMemory().apply {
    insert(marc)
    insert(lea)
  }
  private val findBookshelf = FindBookshelf(bookshelves, readers)

  @Test
  fun `a member reads the bookshelf and every member with their display name and role`() {
    // When
    val answer = findBookshelf(lea.id, familiale.id)

    // Then
    val found = answer.shouldBeInstanceOf<Found>()
    found.bookshelf shouldBe familiale
    found.members shouldContainExactlyInAnyOrder listOf(
      BookshelfMember(marc.id, "Marc", OWNER),
      BookshelfMember(lea.id, "Léa", VIEWER),
    )
  }

  @Test
  fun `a reader who does not belong to the bookshelf is answered no such bookshelf`() {
    // Given
    val tom = readerNamed("tom", "Tom")
    readers.insert(tom)

    // When
    val answer = findBookshelf(tom.id, familiale.id)

    // Then
    answer shouldBe NoSuchBookshelf
  }
}
