package fr.amory.libris.bibliography.infrastructure.lookup

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.cover.CoverSource.OPEN_LIBRARY
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Failed
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Known
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.NothingKnown
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.fixture.recordedBytes
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

class OpenLibrarySourceTest {
  private val source = OpenLibrarySource(server.baseUrl(), "${server.baseUrl()}/b/isbn", TIMEOUT)

  @AfterEach
  fun forgetTheStubs() {
    server.resetAll()
  }

  @Test
  fun `a known ISBN is what Open Library knows about it`() {
    // Given
    openLibrary.knows(SPACE_WARS)

    // When
    val answer = source.lookUpEdition(isbnOf(SPACE_WARS))

    // Then
    answer shouldBe Known(
      EditionPreview(
        isbn = isbnOf(SPACE_WARS),
        kind = BOOK,
        title = "Space Wars - Chapitre 1",
        subtitle = null,
        contributions = Contributions.of(
          listOf(
            Contribution("Baba", WRITER),
            Contribution("Stéphane Lapuss'", WRITER),
            Contribution("Tartuff", WRITER),
          ),
        ),
        series = null,
        collection = null,
        publisher = "KENNES EDITIONS",
        publicationYear = 2020,
        language = null,
        pageCount = 64,
        summary = null,
      ),
      CoverCandidate(OPEN_LIBRARY, "${server.baseUrl()}/b/isbn/9782380751673-L.jpg?default=false"),
    )
  }

  @Test
  fun `looking up an ISBN asks for the edition and one search, never for an author or the cover`() {
    // Given
    openLibrary.knows(SPACE_WARS)

    // When
    source.lookUpEdition(isbnOf(SPACE_WARS))

    // Then
    server.allServeEvents.map { it.request.url } shouldContainExactlyInAnyOrder listOf(
      "/isbn/$SPACE_WARS.json",
      "/books/OL32382513M.json",
      "/search.json?isbn=$SPACE_WARS&fields=key,author_name,edition_key",
    )
  }

  @Test
  fun `the authors come from the work that holds the edition`() {
    // Given
    openLibrary.knows(MONTE_CRISTO)

    // When
    val answer = source.lookUpEdition(isbnOf(MONTE_CRISTO))

    // Then
    answer shouldBe Known(MONTE_CRISTO_EDITION, MONTE_CRISTO_COVER)
  }

  @Test
  fun `a search naming no work holding the edition gives no authors`() {
    // Given
    openLibrary.knows(MONTE_CRISTO)
    openLibrary.answers("/search.json", ANOTHER_WORK)

    // When
    val answer = source.lookUpEdition(isbnOf(MONTE_CRISTO))

    // Then
    answer shouldBe Known(
      MONTE_CRISTO_EDITION.copy(contributions = Contributions.of(emptyList())),
      MONTE_CRISTO_COVER,
    )
  }

  @Test
  fun `an author without a name is left out`() {
    // Given
    openLibrary.knows(MONTE_CRISTO)
    openLibrary.answers("/search.json", A_NAMELESS_AUTHOR)

    // When
    val answer = source.lookUpEdition(isbnOf(MONTE_CRISTO))

    // Then
    val dumas = Contributions.of(listOf(Contribution("Alexandre Dumas", WRITER)))
    answer shouldBe Known(MONTE_CRISTO_EDITION.copy(contributions = dumas), MONTE_CRISTO_COVER)
  }

  @Test
  fun `an ISBN Open Library does not know is nothing known`() {
    // Given
    openLibrary.doesNotKnow(UNKNOWN)

    // When
    val answer = source.lookUpEdition(isbnOf(UNKNOWN))

    // Then
    answer shouldBe NothingKnown
  }

  @Test
  fun `an Open Library that fails on the edition is a failure`() {
    // Given
    openLibrary.fails()

    // When
    val answer = source.lookUpEdition(isbnOf(SPACE_WARS))

    // Then
    answer shouldBe Failed
  }

  @Test
  fun `an Open Library that fails on the search is a failure`() {
    // Given
    openLibrary.knows(SPACE_WARS)
    openLibrary.failsOn("/search.json")

    // When
    val answer = source.lookUpEdition(isbnOf(SPACE_WARS))

    // Then
    answer shouldBe Failed
  }

  @Test
  fun `an answer that cannot be read is a failure`() {
    // Given
    openLibrary.answers("/isbn/$SPACE_WARS.json", "")

    // When
    val answer = source.lookUpEdition(isbnOf(SPACE_WARS))

    // Then
    answer shouldBe Failed
  }

  @Test
  fun `an Open Library that answers past the timeout is a failure`() {
    // Given
    openLibrary.answersTooLate(SPACE_WARS)

    // When
    val answer = source.lookUpEdition(isbnOf(SPACE_WARS))

    // Then
    answer shouldBe Failed
  }

  @Test
  fun `a known ISBN's cover is fetched with the media type it was served with`() {
    // Given
    openLibrary.hasCover(SPACE_WARS, TALL_JPEG)

    // When
    val cover = source.fetchCover(isbnOf(SPACE_WARS))

    // Then
    cover.shouldNotBeNull()
    cover.mediaType shouldBe "image/jpeg"
    cover.bytes shouldBe TALL_JPEG
  }

  @Test
  fun `fetching a cover asks for the cover once, with no default picture, and nothing else`() {
    // Given
    openLibrary.hasCover(SPACE_WARS, TALL_JPEG)

    // When
    source.fetchCover(isbnOf(SPACE_WARS))

    // Then
    server.allServeEvents.map { it.request.url } shouldBe listOf("/b/isbn/$SPACE_WARS-L.jpg?default=false")
  }

  @Test
  fun `a cover Open Library fails to serve is no cover`() {
    // Given
    openLibrary.coverFails(SPACE_WARS)

    // When
    val cover = source.fetchCover(isbnOf(SPACE_WARS))

    // Then
    cover.shouldBeNull()
  }

  @Test
  fun `a cover served with a malformed media type is no cover`() {
    // Given
    openLibrary.hasCover(SPACE_WARS, TALL_JPEG, "jpeg")

    // When
    val cover = source.fetchCover(isbnOf(SPACE_WARS))

    // Then
    cover.shouldBeNull()
  }

  @Test
  fun `a cover Open Library does not have is no cover`() {
    // Given
    openLibrary.hasNoCover(SPACE_WARS)

    // When
    val cover = source.fetchCover(isbnOf(SPACE_WARS))

    // Then
    cover.shouldBeNull()
  }

  private companion object {
    const val SPACE_WARS = "9782380751673"
    const val MONTE_CRISTO = "9782253098058"
    const val UNKNOWN = "9782000000013"
    val TALL_JPEG = recordedBytes("covers/tall.jpg")
    val MONTE_CRISTO_EDITION = EditionPreview(
      isbn = isbnOf(MONTE_CRISTO),
      kind = BOOK,
      title = "Le comte de Monte-Cristo",
      subtitle = "Tome 1",
      contributions = Contributions.of(listOf(Contribution("Alexandre Dumas", WRITER))),
      series = null,
      collection = null,
      publisher = "Le Livre de Poche",
      publicationYear = 2003,
      language = null,
      pageCount = null,
      summary = null,
    )
    val MONTE_CRISTO_COVER
      get() = CoverCandidate(OPEN_LIBRARY, "${server.baseUrl()}/b/isbn/9782253098058-L.jpg?default=false")
    val ANOTHER_WORK = """
            {"docs": [{"key": "/works/OL36287W", "author_name": ["Alexandre Dumas"],
                       "edition_key": ["OL7318447M"]}]}
    """.trimIndent()
    val A_NAMELESS_AUTHOR = """
            {"docs": [{"key": "/works/OL15196753W", "author_name": ["Alexandre Dumas", " "],
                       "edition_key": ["OL50534552M"]}]}
    """.trimIndent()
    val TIMEOUT: Duration = ofMillis(200)
    val WARM_UP_TIMEOUT: Duration = ofSeconds(20)
    val server = WireMockServer(options().dynamicPort())
    val openLibrary = OpenLibraryStubs(server)

    @BeforeAll
    @JvmStatic
    fun startWireMock() {
      server.start()
      openLibrary.knows(SPACE_WARS)
      OpenLibrarySource(server.baseUrl(), "${server.baseUrl()}/b/isbn", WARM_UP_TIMEOUT)
        .lookUpEdition(isbnOf(SPACE_WARS))
      server.resetAll()
    }

    @AfterAll
    @JvmStatic
    fun stopWireMock() {
      server.stop()
    }
  }
}
