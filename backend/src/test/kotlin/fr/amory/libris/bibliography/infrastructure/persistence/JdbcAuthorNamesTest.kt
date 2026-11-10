package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.fixture.JdbcSliceTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@JdbcSliceTest
class JdbcAuthorNamesTest @Autowired constructor(
  private val authorNames: JdbcAuthorNames,
  private val editions: JdbcEditionRepository) {
  @Test
  fun `the name of every author an edition carries is found once`() {
    // Given
    editions.insert(edition("Tintin au Tibet", Contribution("Hergé", WRITER), Contribution("Hergé", ARTIST)))
    editions.insert(
      edition("Astérix le Gaulois", Contribution("René Goscinny", WRITER), Contribution("Albert Uderzo", ARTIST)),
    )

    // When
    val names = authorNames.findAll()

    // Then
    names shouldContainExactlyInAnyOrder listOf("Hergé", "René Goscinny", "Albert Uderzo")
  }

  @Test
  fun `an author no edition carries is not found`() {
    // Given
    val tintin = edition("Tintin au Tibet", Contribution("Hergé", WRITER), Contribution("René Goscinny", WRITER))
    editions.insert(tintin)
    editions.update(tintin.copy(contributions = Contributions.of(listOf(Contribution("Hergé", WRITER)))))

    // When
    val names = authorNames.findAll()

    // Then
    names shouldBe listOf("Hergé")
  }
}

private fun edition(title: String, vararg contributions: Contribution): Edition =
  Edition(
    id = EditionId.new(),
    isbn = null,
    kind = BD,
    title = title,
    subtitle = null,
    contributions = Contributions.of(contributions.toList()),
    seriesVolume = null,
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverName = null,
  )
