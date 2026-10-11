package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.jayway.jsonpath.JsonPath
import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.InventaireStubs
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.bibliography.fixture.recordedBytes
import fr.amory.libris.fixture.MutableClock
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.http.MediaType.IMAGE_JPEG
import org.springframework.http.MediaType.parseMediaType
import org.springframework.test.web.servlet.client.RestTestClient
import java.io.ByteArrayInputStream
import java.time.Duration
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@ScenarioTest
class CoversScenarios @Autowired constructor(
  private val http: RestTestClient,
  private val clock: MutableClock,
  @Qualifier("bnf") bnfServer: WireMockServer,
  @Qualifier("openLibrary") openLibraryServer: WireMockServer,
  @Qualifier("inventaire") inventaireServer: WireMockServer) {
  private val libris = Libris(http)
  private val bnf = BnfStubs(bnfServer)
  private val openLibrary = OpenLibraryStubs(openLibraryServer)
  private val inventaire = InventaireStubs(inventaireServer)
  private val sources = listOf(bnfServer, openLibraryServer, inventaireServer)

  @Test
  fun `S1 The lookup offers the sources' covers`() {
    // Given
    val lea = Reader("lea", "Léa")
    bnf.knows(ONE_PIECE_1)
    openLibrary.knows(ONE_PIECE_1)
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)

    // When
    val response = libris.ask(lea, ONE_PIECE_1)

    // Then
    val body = bodyOf(response.expectStatus().isOk())
    JsonPath.read<List<String>>(body, "$.covers[*].source") shouldBe listOf("inventaire.io", "Open Library", "BnF")
    JsonPath.read<String?>(body, "$.id") shouldBe null
    JsonPath.read<String>(body, "$.covers[0].url") shouldContain ONE_PIECE_1_INVENTAIRE_HASH
    JsonPath.read<String>(body, "$.covers[1].url") shouldContain ONE_PIECE_1
    JsonPath.read<String>(body, "$.covers[2].url") shouldContain ONE_PIECE_1_ARK
    noPictureWasFetched()
  }

  @Test
  fun `S3 The add carries the cover's source`() {
    // Given
    val juliette = Reader("juliette", "Juliette")
    val bookshelf = libris.defaultBookshelfOf(juliette)
    inventaire.knowsButThePictureNeverComes(ONE_PIECE_1)

    // When
    val response: RestTestClient.ResponseSpec
    val elapsed = measureTime { response = libris.add(juliette, bookshelf, onePiece1(coverSource = "inventaire.io")) }

    // Then
    response.expectStatus().isCreated()
    elapsed shouldBeLessThan 2.seconds
    coverOf(juliette, ONE_PIECE_1).shouldBeNull()
  }

  @Test
  fun `S4 The worker fetches the chosen cover`() {
    // Given
    val marc = Reader("marc", "Marc")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    bnf.knows(ONE_PIECE_1)
    bnf.hasCover(TALL_JPEG)
    libris.add(
      marc,
      libris.defaultBookshelfOf(marc),
      onePiece1(coverSource = "inventaire.io")
    ).expectStatus().isCreated()

    // When
    val cover = awaitCover(marc, ONE_PIECE_1)

    // Then
    cover shouldStartWith "/api/v1/covers/"
    servedAs(marc, cover, WEBP)
    inventaire.pictureRequests() shouldBe 1
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 0
    bnf.coverRequests() shouldBe 0
  }

  @Test
  fun `S5 Libris serves a stored cover`() {
    // Given
    val nina = Reader("nina", "Nina")
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    libris.add(
      nina,
      libris.defaultBookshelfOf(nina),
      onePiece1(coverSource = "Open Library")
    ).expectStatus().isCreated()
    val cover = awaitCover(nina, ONE_PIECE_1)

    // When
    val response = picture(nina, cover)

    // Then
    response.expectStatus().isOk()
      .expectHeader().contentType(IMAGE_JPEG)
      .expectHeader().value("Cache-Control") {
        it shouldContain "max-age=31536000"
        it shouldContain "immutable"
      }
  }

  @Test
  fun `S5 Libris serves a stored cover, an address naming no cover`() {
    // Given
    val paul = Reader("paul", "Paul")

    // When
    val response = picture(paul, "/api/v1/covers/${"0".repeat(HASH_LENGTH)}")

    // Then
    response.expectStatus().isNotFound()
      .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.type").isEqualTo("/problems/not-found")
  }

  @Test
  fun `S6 The picture is normalised, taller than 600`() {
    // Given
    val rose = Reader("rose", "Rose")
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    libris.add(
      rose,
      libris.defaultBookshelfOf(rose),
      onePiece1(coverSource = "Open Library")
    ).expectStatus().isCreated()

    // When
    val cover = awaitCover(rose, ONE_PIECE_1)

    // Then
    servedAs(rose, cover, IMAGE_JPEG)
    val stored = ImageIO.read(ByteArrayInputStream(bytesOf(picture(rose, cover).expectStatus().isOk())))
    stored.height shouldBe NORMALISED_HEIGHT
    stored.width shouldBe NORMALISED_HEIGHT * TALL_JPEG_WIDTH / TALL_JPEG_HEIGHT
  }

  @Test
  fun `S6 The picture is normalised, 600 tall or less`() {
    // Given
    val sam = Reader("sam", "Sam")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    libris.add(sam, libris.defaultBookshelfOf(sam), onePiece1(coverSource = "inventaire.io")).expectStatus().isCreated()

    // When
    val cover = awaitCover(sam, ONE_PIECE_1)

    // Then
    val response = picture(sam, cover).expectStatus().isOk()
    response.expectHeader().contentType(WEBP)
    bytesOf(response) shouldBe SMALL_WEBP
  }

  @Test
  fun `S7 An edition without a chosen source gets the cascade, the first source has it`() {
    // Given
    val tom = Reader("tom", "Tom")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    bnf.knows(ONE_PIECE_1)
    bnf.hasCover(TALL_JPEG)
    libris.add(tom, libris.defaultBookshelfOf(tom), onePiece1(coverSource = null)).expectStatus().isCreated()

    // When
    val cover = awaitCover(tom, ONE_PIECE_1)

    // Then
    servedAs(tom, cover, WEBP)
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 0
    bnf.coverRequests() shouldBe 0
  }

  @Test
  fun `S7 An edition without a chosen source gets the cascade, only the last has it`() {
    // Given
    val zoe = Reader("zoe", "Zoé")
    inventaire.doesNotKnow(LES_NERONIA)
    openLibrary.hasNoCover(LES_NERONIA)
    bnf.knows(LES_NERONIA)
    bnf.hasCover(TALL_JPEG)
    libris.add(zoe, libris.defaultBookshelfOf(zoe), lesNeronia(coverSource = null)).expectStatus().isCreated()

    // When
    val cover = awaitCover(zoe, LES_NERONIA)

    // Then
    servedAs(zoe, cover, IMAGE_JPEG)
    openLibrary.coverRequests(LES_NERONIA) shouldBe 1
    bnf.coverRequests() shouldBe 1
  }

  @Test
  fun `S7 An edition without a chosen source gets the cascade, none has it`() {
    // Given
    val eve = Reader("eve", "Ève")
    inventaire.doesNotKnow(LES_NERONIA)
    openLibrary.hasNoCover(LES_NERONIA)
    bnf.knows(LES_NERONIA)
    bnf.hasNoCover()
    libris.add(eve, libris.defaultBookshelfOf(eve), lesNeronia(coverSource = null)).expectStatus().isCreated()

    // When
    await { bnf.coverRequests() == 1 }

    // Then
    openLibrary.coverRequests(LES_NERONIA) shouldBe 1
    coverOf(eve, LES_NERONIA).shouldBeNull()
  }

  @Test
  fun `S8 A failed fetch waits a day, the source does not answer`() {
    openLibrary.coverAnswersTooLate(ONE_PIECE_1)
    aFailedFetchWaitsADay()
  }

  @Test
  fun `S8 A failed fetch waits a day, the source answers an error`() {
    openLibrary.coverFails(ONE_PIECE_1)
    aFailedFetchWaitsADay()
  }

  @Test
  fun `S8 A failed fetch waits a day, the source answers what is not a picture`() {
    openLibrary.coverIsNotAPicture(ONE_PIECE_1)
    aFailedFetchWaitsADay()
  }

  @Test
  fun `S8 A failed fetch waits a day, the source answers a header claiming a side over 5 000 pixels`() {
    openLibrary.hasCover(ONE_PIECE_1, HUGE_HEADER_JPEG)
    aFailedFetchWaitsADay()
  }

  private fun aFailedFetchWaitsADay() {
    // Given
    val luc = Reader("luc", "Luc")
    val bookshelf = libris.defaultBookshelfOf(luc)
    openLibrary.hasCover(ONE_PIECE_2, TALL_JPEG)
    openLibrary.hasCover(ONE_PIECE_3, TALL_JPEG)
    libris.add(luc, bookshelf, onePiece1(coverSource = "Open Library")).expectStatus().isCreated()
    await { openLibrary.coverRequests(ONE_PIECE_1) == 1 }
    coverOf(luc, ONE_PIECE_1).shouldBeNull()

    // When a run within the day, woken by another add
    libris.add(luc, bookshelf, onePiece(ONE_PIECE_2, "Aux prises avec Baggy et ses hommes", 2, "Open Library"))
      .expectStatus().isCreated()
    awaitCover(luc, ONE_PIECE_2)

    // Then
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 1
    coverOf(luc, ONE_PIECE_1).shouldBeNull()

    // When a run a day later
    clock.advance(Duration.ofHours(A_DAY_AND_MORE))
    libris.add(luc, bookshelf, onePiece(ONE_PIECE_3, "Piège", 3, "Open Library")).expectStatus().isCreated()
    awaitCover(luc, ONE_PIECE_3)

    // Then
    await { openLibrary.coverRequests(ONE_PIECE_1) == 2 }
  }

  @Test
  fun `S9 The worker runs on its own`() {
    // Given
    val ana = Reader("ana", "Ana")
    val bookshelf = libris.defaultBookshelfOf(ana)
    openLibrary.hasCoverAfter(ONE_PIECE_1, TALL_JPEG, SLOWLY)
    openLibrary.hasCover(ONE_PIECE_2, TALL_JPEG)

    // When
    libris.add(ana, bookshelf, onePiece1(coverSource = "Open Library")).expectStatus().isCreated()
    libris.add(ana, bookshelf, onePiece(ONE_PIECE_2, "Aux prises avec Baggy et ses hommes", 2, "Open Library"))
      .expectStatus().isCreated()
    awaitCover(ana, ONE_PIECE_1)
    awaitCover(ana, ONE_PIECE_2)

    // Then
    val first = openLibrary.coverRequestTimes(ONE_PIECE_1).shouldHaveSize(1).single()
    val second = openLibrary.coverRequestTimes(ONE_PIECE_2).shouldHaveSize(1).single()
    second.time shouldBeGreaterThanOrEqualTo first.time + SLOWLY
  }

  @Test
  fun `S10 A held edition offers its own cover`() {
    // Given
    val iris = Reader("iris", "Iris")
    val cover = aStoredCover(iris, ONE_PIECE_1)

    // When
    val response = libris.ask(iris, ONE_PIECE_1)

    // Then
    val body = bodyOf(response.expectStatus().isOk())
    JsonPath.read<List<String>>(body, "$.covers[*].source") shouldBe listOf("Libris")
    JsonPath.read<String?>(body, "$.id") shouldNotBe null
    JsonPath.read<String>(body, "$.covers[0].url") shouldBe cover
    noSourceWasAsked()
  }

  @Test
  fun `S10 A held edition offers its own cover, not yet stored`() {
    // Given
    val hugo = Reader("hugo", "Hugo")
    openLibrary.coverAnswersTooLate(ONE_PIECE_1)
    libris.add(
      hugo,
      libris.defaultBookshelfOf(hugo),
      onePiece1(coverSource = "Open Library")
    ).expectStatus().isCreated()

    // When
    val response = libris.ask(hugo, ONE_PIECE_1)

    // Then
    val body = bodyOf(response.expectStatus().isOk())
    JsonPath.read<List<String>>(body, "$.covers[*].source") shouldBe emptyList()
  }

  private fun picture(reader: Reader, path: String): RestTestClient.ResponseSpec =
    http.get()
      .uri(path)
      .headers { it.putAll(reader.headers) }
      .accept(parseMediaType("image/*"), APPLICATION_PROBLEM_JSON)
      .exchange()

  private fun servedAs(reader: Reader, cover: String, type: MediaType) {
    picture(reader, cover).expectStatus().isOk().expectHeader().contentType(type)
  }

  private fun coverOf(reader: Reader, isbn: String): String? =
    JsonPath.read<List<String?>>(
      bodyOf(libris.catalogue(reader).expectStatus().isOk()),
      "$.books[?(@.isbn13 == '$isbn')].coverUrl",
    ).single()

  private fun aStoredCover(reader: Reader, isbn: String): String {
    openLibrary.hasCover(isbn, TALL_JPEG)
    libris.add(reader, libris.defaultBookshelfOf(reader), onePiece(isbn, "Romance dawn", 1, "Open Library"))
      .expectStatus().isCreated()
    val cover = awaitCover(reader, isbn)
    sources.forEach { it.resetRequests() }
    return cover
  }

  private fun awaitCover(reader: Reader, isbn: String): String {
    await { coverOf(reader, isbn) != null }
    return checkNotNull(coverOf(reader, isbn))
  }

  private fun await(condition: () -> Boolean) {
    val deadline = System.nanoTime() + PATIENCE.inWholeNanoseconds
    while (!condition()) {
      check(System.nanoTime() < deadline) { "still not true after $PATIENCE" }
      Thread.sleep(POLL_MILLIS)
    }
  }

  private fun bytesOf(response: RestTestClient.ResponseSpec): ByteArray =
    requireNotNull(response.expectBody(ByteArray::class.java).returnResult().responseBody)

  private fun noPictureWasFetched() {
    inventaire.pictureRequests() shouldBe 0
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 0
    bnf.coverRequests() shouldBe 0
  }

  private fun noSourceWasAsked() {
    sources.forEach { it.verify(0, anyRequestedFor(anyUrl())) }
  }

  private companion object {
    const val ONE_PIECE_1 = "9782723488525"
    const val ONE_PIECE_2 = "9782723489898"
    const val ONE_PIECE_3 = "9782723489904"
    const val LES_NERONIA = "9782505125990"
    const val ONE_PIECE_1_ARK = "cb43636708p"
    const val ONE_PIECE_1_INVENTAIRE_HASH = "34d6e7d99cec5b0922b9eccfeb03748ab2b4db99"
    const val HASH_LENGTH = 64
    const val NORMALISED_HEIGHT = 600
    const val TALL_JPEG_WIDTH = 800
    const val TALL_JPEG_HEIGHT = 1200
    const val SLOWLY = 500
    const val POLL_MILLIS = 100L
    const val A_DAY_AND_MORE = 25L
    val PATIENCE = 5.seconds
    val WEBP: MediaType = parseMediaType("image/webp")
    val TALL_JPEG = recordedBytes("covers/tall.jpg")
    val SMALL_WEBP = recordedBytes("covers/small.webp")
    val HUGE_HEADER_JPEG = recordedBytes("covers/huge-header.jpg")

    fun onePiece1(coverSource: String?) =
      onePiece(ONE_PIECE_1, "Romance dawn", 1, coverSource)

    fun onePiece(isbn: String, title: String, volume: Int, coverSource: String?) =
      edition(
        isbn = isbn,
        kind = "MANGA",
        title = title,
        authorsAndSeries = """
                "authors": [{ "name": "Eiichirō Oda", "role": "WRITER" }, { "name": "Eiichirō Oda", "role": "ARTIST" }],
                "series": { "name": "One piece", "volumeNumber": $volume }
        """.trimIndent(),
        coverSource = coverSource,
      )

    fun lesNeronia(coverSource: String?) =
      edition(
        isbn = LES_NERONIA,
        kind = "BD",
        title = "Les Neronia",
        authorsAndSeries = """
                "authors": [{ "name": "Jean Dufaux", "role": "WRITER" }, { "name": "Jérémy", "role": "ARTIST" }],
                "series": null
        """.trimIndent(),
        coverSource = coverSource,
      )

    fun edition(isbn: String, kind: String, title: String, authorsAndSeries: String, coverSource: String?) =
      """
            {
              "isbn13": "$isbn",
              "kind": "$kind",
              "title": "$title",
              "subtitle": null,
              $authorsAndSeries,
              "collection": null,
              "publisher": null,
              "publicationYear": null,
              "language": "fr",
              "pageCount": null,
              "summary": null,
              "coverSource": ${coverSource?.let { "\"$it\"" } ?: "null"}
            }
      """.trimIndent()
  }
}
