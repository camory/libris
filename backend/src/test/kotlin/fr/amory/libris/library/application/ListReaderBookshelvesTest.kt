package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ListReaderBookshelvesTest {
  @Test
  fun `a reader's bookshelves are answered with their role on each, and none they are no member of`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val marc = readerNamed("marc", "Marc")
    val salon = Bookshelf(
      BookshelfId.new(),
      BookshelfName("Salon"),
      listOf(Membership(marc.id, OWNER), Membership(lea.id, VIEWER)),
    )
    val bookshelves = BookshelvesInMemory()
    bookshelves.insert(bookshelfOwnedBy(lea))
    bookshelves.insert(salon)
    bookshelves.insert(bookshelfOwnedBy(marc))
    val listReaderBookshelves = ListReaderBookshelves(bookshelves)

    // When
    val answer = listReaderBookshelves(lea)

    // Then
    answer shouldContainExactlyInAnyOrder listOf(
      ReaderBookshelf(lea.defaultBookshelfId, "Bibliothèque de Léa", OWNER),
      ReaderBookshelf(salon.id, "Salon", VIEWER),
    )
  }

  @Test
  fun `the reader's default comes first, though its name sorts after the others'`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val albums = Bookshelf(BookshelfId.new(), BookshelfName("Albums"), listOf(Membership(lea.id, OWNER)))
    val bookshelves = BookshelvesInMemory()
    bookshelves.insert(albums)
    bookshelves.insert(bookshelfOwnedBy(lea))
    val listReaderBookshelves = ListReaderBookshelves(bookshelves)

    // When
    val answer = listReaderBookshelves(lea)

    // Then
    answer shouldBe listOf(
      ReaderBookshelf(lea.defaultBookshelfId, "Bibliothèque de Léa", OWNER),
      ReaderBookshelf(albums.id, "Albums", OWNER),
    )
  }

  @Test
  fun `the others follow by name, ignoring case`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val bandesDessinees =
      Bookshelf(BookshelfId.new(), BookshelfName("Bandes dessinées"), listOf(Membership(lea.id, OWNER)))
    val albums = Bookshelf(BookshelfId.new(), BookshelfName("albums"), listOf(Membership(lea.id, OWNER)))
    val bookshelves = BookshelvesInMemory()
    bookshelves.insert(bookshelfOwnedBy(lea))
    bookshelves.insert(bandesDessinees)
    bookshelves.insert(albums)
    val listReaderBookshelves = ListReaderBookshelves(bookshelves)

    // When
    val answer = listReaderBookshelves(lea)

    // Then
    answer shouldBe listOf(
      ReaderBookshelf(lea.defaultBookshelfId, "Bibliothèque de Léa", OWNER),
      ReaderBookshelf(albums.id, "albums", OWNER),
      ReaderBookshelf(bandesDessinees.id, "Bandes dessinées", OWNER),
    )
  }

  @Test
  fun `the others follow by name, ignoring accents but not spaces`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val albums = Bookshelf(BookshelfId.new(), BookshelfName("Albums"), listOf(Membership(lea.id, OWNER)))
    val aLire = Bookshelf(BookshelfId.new(), BookshelfName("À lire"), listOf(Membership(lea.id, OWNER)))
    val bookshelves = BookshelvesInMemory()
    bookshelves.insert(bookshelfOwnedBy(lea))
    bookshelves.insert(albums)
    bookshelves.insert(aLire)
    val listReaderBookshelves = ListReaderBookshelves(bookshelves)

    // When
    val answer = listReaderBookshelves(lea)

    // Then
    answer shouldBe listOf(
      ReaderBookshelf(lea.defaultBookshelfId, "Bibliothèque de Léa", OWNER),
      ReaderBookshelf(aLire.id, "À lire", OWNER),
      ReaderBookshelf(albums.id, "Albums", OWNER),
    )
  }

  @Test
  fun `the others follow by name, reading œ as oe and æ as ae`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val ogres = Bookshelf(BookshelfId.new(), BookshelfName("Ogres"), listOf(Membership(lea.id, OWNER)))
    val oeuvres = Bookshelf(BookshelfId.new(), BookshelfName("Œuvres"), listOf(Membership(lea.id, OWNER)))
    val albums = Bookshelf(BookshelfId.new(), BookshelfName("Albums"), listOf(Membership(lea.id, OWNER)))
    val aesop = Bookshelf(BookshelfId.new(), BookshelfName("Æsop"), listOf(Membership(lea.id, OWNER)))
    val bookshelves = BookshelvesInMemory()
    bookshelves.insert(bookshelfOwnedBy(lea))
    bookshelves.insert(ogres)
    bookshelves.insert(oeuvres)
    bookshelves.insert(albums)
    bookshelves.insert(aesop)
    val listReaderBookshelves = ListReaderBookshelves(bookshelves)

    // When
    val answer = listReaderBookshelves(lea)

    // Then
    answer shouldBe listOf(
      ReaderBookshelf(lea.defaultBookshelfId, "Bibliothèque de Léa", OWNER),
      ReaderBookshelf(aesop.id, "Æsop", OWNER),
      ReaderBookshelf(albums.id, "Albums", OWNER),
      ReaderBookshelf(oeuvres.id, "Œuvres", OWNER),
      ReaderBookshelf(ogres.id, "Ogres", OWNER),
    )
  }
}
