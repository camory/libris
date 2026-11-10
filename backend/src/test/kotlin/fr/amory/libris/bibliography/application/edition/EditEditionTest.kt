package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.application.edition.EditEditionResult.NoSuchEdition
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EditEditionTest {
  private val editions = EditionsInMemory()
  private val editEdition = EditEdition()

  @Test
  fun `an edition the house lacks is no such edition`() {
    // Given
    editions.insert(asterixLeGaulois())

    // When
    val result = editEdition(EditionId.new(), laSerpeDOr())

    // Then
    result shouldBe NoSuchEdition
    editions.stored shouldBe listOf(asterixLeGaulois())
  }

  private fun asterixLeGaulois(): Edition =
    Edition(
      id = ASTERIX_LE_GAULOIS,
      isbn = isbnOf("9782012101333"),
      kind = BD,
      title = "Astérix le Gaulois",
      subtitle = null,
      contributions = Contributions.of(listOf(Contribution("René Goscinny", WRITER))),
      seriesVolume = null,
      collection = null,
      publisher = "Hachette",
      publicationYear = 1999,
      language = "fr",
      pageCount = 48,
      summary = null,
      coverName = null,
    )

  private fun laSerpeDOr(): EditionDetails =
    EditionDetails(
      kind = BD,
      title = "La Serpe d'or",
      subtitle = null,
      contributions = Contributions.of(listOf(Contribution("Albert Uderzo", ARTIST))),
      series = null,
      collection = null,
      publisher = "Hachette",
      publicationYear = 2004,
      language = "fr",
      pageCount = 48,
      summary = null,
    )
}

private val ASTERIX_LE_GAULOIS = EditionId.new()
