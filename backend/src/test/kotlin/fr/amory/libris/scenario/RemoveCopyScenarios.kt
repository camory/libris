package fr.amory.libris.scenario

import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.bibliography.fixture.recordedBytes
import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NOT_FOUND
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.Role.OWNER
import fr.amory.libris.scenario.dsl.Role.VIEWER
import fr.amory.libris.scenario.dsl.SeriesEntry
import fr.amory.libris.scenario.dsl.Sources
import fr.amory.libris.scenario.dsl.asks
import fr.amory.libris.scenario.dsl.awaitsCoverOf
import fr.amory.libris.scenario.dsl.catalogue
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.hasCreated
import fr.amory.libris.scenario.dsl.opens
import fr.amory.libris.scenario.dsl.removes
import fr.amory.libris.scenario.dsl.rolesOn
import fr.amory.libris.scenario.dsl.unknownCopy
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class RemoveCopyScenarios @Autowired constructor(
  private val libris: Libris,
  private val sources: Sources,
  private val openLibrary: OpenLibraryStubs) {

  @Test
  fun `S1 The owner removes one copy of two`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val (first, second) = lea.hasAdded(ASTERIX_LE_GAULOIS, ASTERIX_LE_GAULOIS, to = lea.defaultBookshelf)
    val book = lea.catalogue().books.single()

    // When
    val answer = lea.removes(first)

    // Then
    answer.refusal.shouldBeNull()
    lea.opens(book).value.copies shouldBe listOf(second)
  }

  @Test
  fun `S2 The reader removes their last copy, the house's last`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    openLibrary.hasCover(ASTERIX_LE_GAULOIS.isbn13, TALL_JPEG)
    val copy = lea.hasAdded(ASTERIX_LE_GAULOIS, to = lea.defaultBookshelf)
    lea.awaitsCoverOf(ASTERIX_LE_GAULOIS.isbn13)
    val book = lea.catalogue().books.single()
    sources.forgetRequests()

    // When
    val answer = lea.removes(copy)

    // Then
    answer.refusal.shouldBeNull()
    lea.opens(book).refusal shouldBe NOT_FOUND
    val card = lea.asks(ASTERIX_LE_GAULOIS.isbn13).value
    card.id shouldBe book.id
    card.title shouldBe "Astérix le Gaulois"
    card.copies.shouldBeEmpty()
    sources.wereNotAsked()
  }

  @Test
  fun `S2 The reader removes their last copy, one left on another reader's bookshelf`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    val leasCopy = lea.hasAdded(ASTERIX_LE_GAULOIS, to = lea.defaultBookshelf)
    val samsCopy = sam.hasAdded(ASTERIX_LE_GAULOIS, to = sam.defaultBookshelf)

    // When
    val answer = lea.removes(leasCopy)

    // Then
    answer.refusal.shouldBeNull()
    sam.catalogue().books.single().copies shouldBe listOf(samsCopy)
  }

  @Test
  fun `S3 A guest's row has no trash, a copy on a bookshelf the reader joins as a guest`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    val salon = sam.hasCreated("Salon")
    sam.hasAdded(lea, VIEWER, to = salon)
    val leasCopy = lea.hasAdded(ASTERIX_LE_GAULOIS, to = lea.defaultBookshelf)
    val salonsCopy = sam.hasAdded(ASTERIX_LE_GAULOIS, to = salon)
    val book = lea.catalogue().books.single()

    // When
    val roles = lea.rolesOn(book)

    // Then
    roles shouldBe mapOf(leasCopy to OWNER, salonsCopy to VIEWER)

    // When
    val answer = lea.removes(salonsCopy)

    // Then
    answer.refusal shouldBe NOT_FOUND
    lea.opens(book).value.copies shouldContainExactlyInAnyOrder listOf(leasCopy, salonsCopy)
  }

  @Test
  fun `S3 A guest's row has no trash, a copy on a bookshelf the reader does not belong to`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    val samsCopy = sam.hasAdded(ASTERIX_LE_GAULOIS, to = sam.defaultBookshelf)

    // When
    val answer = lea.removes(samsCopy)

    // Then
    answer.refusal shouldBe NOT_FOUND
    sam.catalogue().books.single().copies shouldBe listOf(samsCopy)
  }

  @Test
  fun `S3 A guest's row has no trash, a copy Libris does not know`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val copy = libris.unknownCopy()

    // When
    val answer = lea.removes(copy)

    // Then
    answer.refusal shouldBe NOT_FOUND
  }

  private companion object {
    val ASTERIX_LE_GAULOIS =
      NewBook(
        isbn13 = "9782012101333",
        kind = "BD",
        title = "Astérix le Gaulois",
        authors = listOf(Author("René Goscinny", "WRITER"), Author("Albert Uderzo", "ARTIST")),
        series = SeriesEntry("Astérix", 1),
        language = "fr",
        coverSource = "Open Library",
      )
    val TALL_JPEG = recordedBytes("covers/tall.jpg")
  }
}
