package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.JdbcSliceTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.simple.JdbcClient

private const val ONE_PIECE = "9782723488525"
private const val ONE_PIECE_TOME_TWO = "9782723489898"

@JdbcSliceTest
@Import(JdbcAwaitedCoverRepository::class, JdbcEditionRepository::class)
class JdbcAwaitedCoverRepositoryTest @Autowired constructor(
  private val awaitedCovers: JdbcAwaitedCoverRepository,
  private val editions: JdbcEditionRepository,
  private val jdbcClient: JdbcClient) {
  @Test
  fun `an awaited cover is stored with the name of its chosen source`() {
    // Given
    editions.insert(onePieceTomeOne())

    // When
    awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))

    // Then
    sourcesAwaitedFor(ONE_PIECE) shouldBe listOf("inventaire.io")
  }

  @Test
  fun `an awaited cover with no chosen source is stored with none`() {
    // Given
    editions.insert(onePieceTomeOne())

    // When
    awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE), null))

    // Then
    sourcesAwaitedFor(ONE_PIECE) shouldBe listOf(null)
  }

  @Test
  fun `an awaited cover of an ISBN no edition bears is refused`() {
    shouldThrow<DataIntegrityViolationException> {
      awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))
    }
  }

  @Test
  fun `the awaited covers are read back with their chosen source, or none`() {
    // Given
    editions.insert(onePieceTomeOne())
    editions.insert(onePieceTomeOne().copy(id = EditionId.new(), isbn = isbnOf(ONE_PIECE_TOME_TWO)))
    awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))
    awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE_TOME_TWO), null))

    // When
    val found = awaitedCovers.findAll()

    // Then
    found shouldContainExactlyInAnyOrder listOf(
      AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE),
      AwaitedCover(isbnOf(ONE_PIECE_TOME_TWO), null),
    )
  }

  @Test
  fun `a deleted awaited cover is awaited no more`() {
    // Given
    editions.insert(onePieceTomeOne())
    editions.insert(onePieceTomeOne().copy(id = EditionId.new(), isbn = isbnOf(ONE_PIECE_TOME_TWO)))
    awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))
    awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE_TOME_TWO), null))

    // When
    awaitedCovers.delete(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))

    // Then
    awaitedCovers.findAll() shouldBe listOf(AwaitedCover(isbnOf(ONE_PIECE_TOME_TWO), null))
  }

  private fun sourcesAwaitedFor(isbn13: String): List<String?> =
    jdbcClient
      .sql("SELECT awaited_cover.source FROM awaited_cover WHERE awaited_cover.isbn13 = :isbn13")
      .param("isbn13", isbn13)
      .query { rs, _ -> rs.getString("source") }
      .list()

  private fun onePieceTomeOne(): Edition =
    Edition(
      id = EditionId.new(),
      isbn = isbnOf(ONE_PIECE),
      kind = MANGA,
      title = "Romance dawn",
      subtitle = null,
      contributions = Contributions.of(emptyList()),
      series = null,
      collection = null,
      publisher = null,
      publicationYear = null,
      language = null,
      pageCount = null,
      summary = null,
      coverName = null,
    )
}
