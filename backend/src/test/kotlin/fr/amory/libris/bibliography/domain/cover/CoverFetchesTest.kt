package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.cover.CoverSource.BNF
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.cover.CoverSource.OPEN_LIBRARY
import fr.amory.libris.bibliography.fixture.CoverFetchAnswering
import fr.amory.libris.bibliography.fixture.coverOf
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.fixture.pictureOf
import fr.amory.libris.bibliography.fixture.recordedBytes
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

private const val ONE_PIECE = "9782723488525"

class CoverFetchesTest {
  private val webp = coverOf("image/webp", recordedBytes("covers/small.webp"))
  private val jpeg = coverOf("image/jpeg", pictureOf("jpeg", 400, 600))

  @Test
  fun `with a chosen source, its cover alone is fetched`() {
    // Given
    val inventaire = CoverFetchAnswering(INVENTAIRE, webp)
    val coverFetches = CoverFetches.of(listOf(inventaire, CoverFetchAnswering(OPEN_LIBRARY, jpeg)))

    // When
    val cover = coverFetches.coverFor(isbnOf(ONE_PIECE), OPEN_LIBRARY)

    // Then
    cover shouldBe jpeg
    inventaire.asked.shouldBeEmpty()
  }

  @Test
  fun `with no chosen source, the first cover of inventaire io, Open Library, then the BnF is fetched`() {
    // Given
    val inventaire = CoverFetchAnswering(INVENTAIRE, null)
    val bnf = CoverFetchAnswering(BNF, jpeg)
    val coverFetches = CoverFetches.of(listOf(bnf, inventaire, CoverFetchAnswering(OPEN_LIBRARY, webp)))

    // When
    val cover = coverFetches.coverFor(isbnOf(ONE_PIECE), null)

    // Then
    cover shouldBe webp
    inventaire.asked shouldBe listOf(isbnOf(ONE_PIECE))
    bnf.asked.shouldBeEmpty()
  }

  @Test
  fun `with no chosen source, a cover that is no picture is passed for the next source's`() {
    // Given
    val openLibrary = CoverFetchAnswering(OPEN_LIBRARY, jpeg)
    val coverFetches =
      CoverFetches.of(listOf(CoverFetchAnswering(INVENTAIRE, coverOf("image/png", byteArrayOf(1, 2, 3))), openLibrary))

    // When
    val cover = coverFetches.coverFor(isbnOf(ONE_PIECE), null)

    // Then
    cover shouldBe jpeg
    openLibrary.asked shouldBe listOf(isbnOf(ONE_PIECE))
  }
}
