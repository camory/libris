package fr.amory.libris.scenario

import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NOT_FOUND
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.Role.VIEWER
import fr.amory.libris.scenario.dsl.SeriesEntry
import fr.amory.libris.scenario.dsl.catalogue
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.hasCreated
import fr.amory.libris.scenario.dsl.opens
import fr.amory.libris.scenario.dsl.unknownBook
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class EditionPageScenarios @Autowired constructor(private val libris: Libris) {

  @Test
  fun `S1 The reader opens an ouvrage`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    val salon = sam.hasCreated("Salon")
    sam.hasAdded(lea, VIEWER, to = salon)

    val leasCopy = lea.hasAdded(ASTERIX_LE_GAULOIS, to = lea.defaultBookshelf)
    val salonsCopy = sam.hasAdded(ASTERIX_LE_GAULOIS, to = salon)
    sam.hasAdded(ASTERIX_LE_GAULOIS, to = sam.defaultBookshelf)

    // When
    val answer = lea.opens(lea.catalogue().books.single())

    // Then
    val book = answer.value
    book.title shouldBe "Astérix le Gaulois"
    book.copies shouldContainExactlyInAnyOrder listOf(leasCopy, salonsCopy)
  }

  @Test
  fun `S2 No such ouvrage, an edition Libris does not know`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val book = libris.unknownBook()

    // When
    val answer = lea.opens(book)

    // Then
    answer.refusal shouldBe NOT_FOUND
  }

  @Test
  fun `S2 No such ouvrage, one only on bookshelves the reader does not belong to`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    sam.hasAdded(ASTERIX_LE_GAULOIS, to = sam.defaultBookshelf)
    val book = sam.catalogue().books.single()

    // When
    val answer = lea.opens(book)

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
      )
  }
}
