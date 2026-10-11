package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.jayway.jsonpath.JsonPath
import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.bibliography.fixture.recordedBytes
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.client.RestTestClient
import kotlin.time.Duration.Companion.seconds

@ScenarioTest
class BookshelfScenarios @Autowired constructor(
  http: RestTestClient,
  @Qualifier("bnf") bnfServer: WireMockServer,
  @Qualifier("openLibrary") openLibraryServer: WireMockServer) {
  private val libris = Libris(http)
  private val bnf = BnfStubs(bnfServer)
  private val openLibrary = OpenLibraryStubs(openLibraryServer)
  private val sources = listOf(bnfServer, openLibraryServer)

  @Test
  fun `S1 The first visit creates the bookshelf`() {
    // Given
    val lea = Reader("lea", "Léa")
    // When
    val response = libris.me(lea)
    // Then
    response.expectStatus().isOk()
      .expectBody()
      .jsonPath("$.defaultBookshelf.id").isNotEmpty()
      .jsonPath("$.defaultBookshelf.name").isEqualTo("Bibliothèque de Léa")
  }

  @Test
  fun `S2 The ouvrage is added`() {
    // Given
    val juliette = Reader("juliette", "Juliette")
    bnf.knows("9782723488525")
    libris.ask(juliette, "9782723488525").expectStatus().isOk()
      .expectBody().jsonPath("$.copies").isEmpty()
    val bookshelf = libris.defaultBookshelfOf(juliette)
    // When
    val response = libris.add(juliette, bookshelf, NEW_ONE_PIECE_1)
    // Then
    response.expectStatus().isCreated()
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isNotEmpty()
      .jsonPath("$.bookshelf.id").isEqualTo(bookshelf)
      .jsonPath("$.bookshelf.name").isEqualTo("Bibliothèque de Juliette")
    libris.ask(juliette, "9782723488525").expectStatus().isOk()
      .expectBody()
      .jsonPath("$.kind").isEqualTo("MANGA")
      .jsonPath("$.copies.length()").isEqualTo(1)
      .jsonPath("$.copies[0].bookshelf.name").isEqualTo("Bibliothèque de Juliette")
  }

  @Test
  fun `S3 A known ISBN reaches the existing edition, another reader`() {
    // Given
    val marc = Reader("marc", "Marc")
    val nina = Reader("nina", "Nina")
    libris.add(marc, libris.defaultBookshelfOf(marc), NEW_ONE_PIECE_2).expectStatus().isCreated()
    // When
    val response = libris.add(nina, libris.defaultBookshelfOf(nina), NEW_ONE_PIECE_2)
    // Then
    response.expectStatus().isCreated()
      .expectBody().jsonPath("$.bookshelf.name").isEqualTo("Bibliothèque de Nina")
    libris.ask(nina, "9782723489898").expectStatus().isOk()
      .expectBody()
      .jsonPath("$.title").isEqualTo("Aux prises avec Baggy et ses hommes")
      .jsonPath("$.copies.length()").isEqualTo(1)
      .jsonPath("$.copies[0].bookshelf.name").isEqualTo("Bibliothèque de Nina")
  }

  @Test
  fun `S3 A known ISBN reaches the existing edition, the same reader again`() {
    // Given
    val paul = Reader("paul", "Paul")
    val bookshelf = libris.defaultBookshelfOf(paul)
    val first = idOf(libris.add(paul, bookshelf, NEW_ONE_PIECE_2).expectStatus().isCreated())
    // When
    val response = libris.add(paul, bookshelf, NEW_ONE_PIECE_2)
    // Then
    val second = idOf(response.expectStatus().isCreated())
    second shouldNotBe first
    libris.ask(paul, "9782723489898").expectStatus().isOk()
      .expectBody()
      .jsonPath("$.copies.length()").isEqualTo(2)
      .jsonPath("$.copies[*].bookshelf.name").isEqualTo(listOf("Bibliothèque de Paul", "Bibliothèque de Paul"))
  }

  @Test
  fun `S4 The ouvrage is already in a bookshelf`() {
    // Given
    val rose = Reader("rose", "Rose")
    val sam = Reader("sam", "Sam")
    val rosesCopy = addedWithItsCover(rose, NEW_ONE_PIECE_3, ONE_PIECE_3)
    libris.add(sam, libris.defaultBookshelfOf(sam), NEW_ONE_PIECE_3).expectStatus().isCreated()
    // When
    val response = libris.ask(rose, ONE_PIECE_3)
    // Then
    response.expectStatus().isOk()
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.title").isEqualTo("Piège")
      .jsonPath("$.kind").isEqualTo("MANGA")
      .jsonPath("$.copies.length()").isEqualTo(1)
      .jsonPath("$.copies[0].id").isEqualTo(rosesCopy)
      .jsonPath("$.copies[0].bookshelf.name").isEqualTo("Bibliothèque de Rose")
    noSourceWasAsked()
  }

  @Test
  fun `S4 The ouvrage is already in a bookshelf, none of the reader's`() {
    // Given
    val tom = Reader("tom", "Tom")
    val zoe = Reader("zoe", "Zoé")
    addedWithItsCover(tom, NEW_ONE_PIECE_3, ONE_PIECE_3)
    // When
    val response = libris.ask(zoe, ONE_PIECE_3)
    // Then
    response.expectStatus().isOk()
      .expectBody()
      .jsonPath("$.title").isEqualTo("Piège")
      .jsonPath("$.copies").isEmpty()
    noSourceWasAsked()
  }

  private fun addedWithItsCover(reader: Reader, book: String, isbn: String): String {
    openLibrary.hasCover(isbn, TALL_JPEG)
    val copy = idOf(libris.add(reader, libris.defaultBookshelfOf(reader), book).expectStatus().isCreated())
    await { coverOf(reader, isbn) != null }
    sources.forEach { it.resetRequests() }
    return copy
  }

  private fun coverOf(reader: Reader, isbn: String): String? =
    JsonPath.read<List<String?>>(
      bodyOf(libris.catalogue(reader).expectStatus().isOk()),
      "$.books[?(@.isbn13 == '$isbn')].coverUrl",
    ).single()

  private fun await(condition: () -> Boolean) {
    val deadline = System.nanoTime() + PATIENCE.inWholeNanoseconds
    while (!condition()) {
      check(System.nanoTime() < deadline) { "still not true after $PATIENCE" }
      Thread.sleep(POLL_MILLIS)
    }
  }

  private fun idOf(response: RestTestClient.ResponseSpec): String =
    JsonPath.read(bodyOf(response), "$.id")

  private fun noSourceWasAsked() {
    sources.forEach { it.verify(0, anyRequestedFor(anyUrl())) }
  }

  private companion object {
    const val ONE_PIECE_3 = "9782723489904"
    const val POLL_MILLIS = 100L
    val PATIENCE = 5.seconds
    val TALL_JPEG = recordedBytes("covers/tall.jpg")
    val NEW_ONE_PIECE_1 = onePiece(
      isbn = "9782723488525",
      title = "Romance dawn",
      subtitle = "\"à l'aube d'une grande aventure\"",
      volume = 1,
    )
    val NEW_ONE_PIECE_2 = onePiece(
      isbn = "9782723489898",
      title = "Aux prises avec Baggy et ses hommes",
      subtitle = "null",
      volume = 2,
    )
    val NEW_ONE_PIECE_3 = onePiece(
      isbn = ONE_PIECE_3,
      title = "Piège",
      subtitle = "null",
      volume = 3,
      coverSource = "Open Library",
    )

    fun onePiece(isbn: String, title: String, subtitle: String, volume: Int, coverSource: String? = null) =
      """
            {
              "isbn13": "$isbn",
              "kind": "MANGA",
              "title": "$title",
              "subtitle": $subtitle,
              "authors": [
                { "name": "Eiichirō Oda", "role": "WRITER" },
                { "name": "Eiichirō Oda", "role": "ARTIST" }
              ],
              "series": { "name": "One piece", "volumeNumber": $volume },
              "collection": "Shonen manga",
              "publisher": "Glénat",
              "publicationYear": 2013,
              "language": "fr",
              "pageCount": null,
              "summary": null,
              "coverSource": ${coverSource?.let { "\"$it\"" } ?: "null"}
            }
      """.trimIndent()
  }
}
