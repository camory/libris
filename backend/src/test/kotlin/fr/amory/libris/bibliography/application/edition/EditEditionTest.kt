package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.application.edition.EditEditionResult.NoSuchEdition
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EditEditionTest {
  private val editions = EditionsInMemory()
  private val editEdition = EditEdition(editions)

  @Test
  fun `an edition the house lacks is no such edition`() {
    // Given
    editions.insert(asterixLeGaulois())

    // When
    val result = editEdition(EditionId.new(), asEdited())

    // Then
    result shouldBe NoSuchEdition
    editions.stored shouldBe listOf(asterixLeGaulois())
  }

  @Test
  fun `the held edition is written with the details sent, its id, ISBN and cover name kept`() {
    // Given
    editions.insert(asterixLeGaulois())

    // When
    val result = editEdition(ASTERIX_LE_GAULOIS, asEdited())

    // Then
    val edited = Edition(
      id = ASTERIX_LE_GAULOIS,
      isbn = isbnOf("9782012101333"),
      kind = BD,
      title = "Astérix le Gaulois",
      subtitle = "Une aventure d'Astérix",
      contributions = Contributions.of(
        listOf(Contribution("René Goscinny", WRITER), Contribution("Albert Uderzo", ARTIST)),
      ),
      seriesVolume = null,
      collection = "Les aventures d'Astérix",
      publisher = "Dargaud",
      publicationYear = 1961,
      language = "fr",
      pageCount = 48,
      summary = "Nous sommes en 50 avant Jésus-Christ.",
      coverName = ASTERIX_COVER,
    )
    result shouldBe Edited(edited)
    editions.stored shouldBe listOf(edited)
  }

  private fun asterixLeGaulois(): Edition =
    Edition(
      id = ASTERIX_LE_GAULOIS,
      isbn = isbnOf("9782012101333"),
      kind = BOOK,
      title = "Asterix le gaulois",
      subtitle = null,
      contributions = Contributions.of(listOf(Contribution("René Goscinny", WRITER))),
      seriesVolume = SeriesVolume(ASTERIX, 1),
      collection = null,
      publisher = "Hachette",
      publicationYear = 1999,
      language = null,
      pageCount = null,
      summary = null,
      coverName = ASTERIX_COVER,
    )

  private fun asEdited(): EditionDetails =
    EditionDetails(
      kind = BD,
      title = "Astérix le Gaulois",
      subtitle = "Une aventure d'Astérix",
      contributions = Contributions.of(
        listOf(Contribution("René Goscinny", WRITER), Contribution("Albert Uderzo", ARTIST)),
      ),
      series = null,
      collection = "Les aventures d'Astérix",
      publisher = "Dargaud",
      publicationYear = 1961,
      language = "fr",
      pageCount = 48,
      summary = "Nous sommes en 50 avant Jésus-Christ.",
    )
}

private val ASTERIX_LE_GAULOIS = EditionId.new()
private val ASTERIX = SeriesId.new()
private val ASTERIX_COVER = CoverName("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
