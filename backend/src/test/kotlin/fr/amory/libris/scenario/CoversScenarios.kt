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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
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
  private val bnf = BnfStubs(bnfServer)
  private val openLibrary = OpenLibraryStubs(openLibraryServer)
  private val inventaire = InventaireStubs(inventaireServer)
  private val sources = listOf(bnfServer, openLibraryServer, inventaireServer)

  @Test
  fun `S1 The lookup offers the sources' covers`() {
    // Given
    val lea = reader("lea", "Léa")
    bnf.knows(ONE_PIECE_1)
    openLibrary.knows(ONE_PIECE_1)
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)

    // When
    val response = ask(lea, ONE_PIECE_1)

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
  @Disabled("covers")
  fun `S3 The add carries the cover's source`() {
    // Given
    val juliette = reader("juliette", "Juliette")
    val bookshelf = defaultBookshelfOf(juliette)
    inventaire.knowsButThePictureNeverComes(ONE_PIECE_1)

    // When
    val response: RestTestClient.ResponseSpec
    val elapsed = measureTime { response = add(juliette, bookshelf, onePiece1(coverSource = "inventaire.io")) }

    // Then
    response.expectStatus().isCreated()
    elapsed shouldBeLessThan 2.seconds
    coverOf(juliette, ONE_PIECE_1).shouldBeNull()
  }

  @Test
  @Disabled("covers")
  fun `S4 The worker fetches the chosen cover`() {
    // Given
    val marc = reader("marc", "Marc")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    bnf.knows(ONE_PIECE_1)
    bnf.hasCover(TALL_JPEG)
    add(marc, defaultBookshelfOf(marc), onePiece1(coverSource = "inventaire.io")).expectStatus().isCreated()

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
  @Disabled("covers")
  fun `S5 Libris serves a stored cover`() {
    // Given
    val nina = reader("nina", "Nina")
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    add(nina, defaultBookshelfOf(nina), onePiece1(coverSource = "Open Library")).expectStatus().isCreated()
    val cover = awaitCover(nina, ONE_PIECE_1)

    // When
    val response = picture(nina, cover)

    // Then
    response
      .expectStatus()
      .isOk()
      .expectHeader()
      .contentType(IMAGE_JPEG)
      .expectHeader()
      .value("Cache-Control") {
        it shouldContain "max-age=31536000"
        it shouldContain "immutable"
      }
  }

  @Test
  fun `S5 Libris serves a stored cover, an address naming no cover`() {
    // Given
    val paul = reader("paul", "Paul")

    // When
    val response = picture(paul, "/api/v1/covers/${"0".repeat(HASH_LENGTH)}")

    // Then
    response
      .expectStatus()
      .isNotFound()
      .expectHeader()
      .contentType(APPLICATION_PROBLEM_JSON)
      .expectBody()
      .jsonPath("$.type")
      .isEqualTo("/problems/not-found")
  }

  @Test
  @Disabled("covers")
  fun `S6 The picture is normalised, taller than 600`() {
    // Given
    val rose = reader("rose", "Rose")
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    add(rose, defaultBookshelfOf(rose), onePiece1(coverSource = "Open Library")).expectStatus().isCreated()

    // When
    val cover = awaitCover(rose, ONE_PIECE_1)

    // Then
    servedAs(rose, cover, IMAGE_JPEG)
    val stored = ImageIO.read(ByteArrayInputStream(bytesOf(picture(rose, cover).expectStatus().isOk())))
    stored.height shouldBe NORMALISED_HEIGHT
    stored.width shouldBe NORMALISED_HEIGHT * TALL_JPEG_WIDTH / TALL_JPEG_HEIGHT
  }

  @Test
  @Disabled("covers")
  fun `S6 The picture is normalised, 600 tall or less`() {
    // Given
    val sam = reader("sam", "Sam")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    add(sam, defaultBookshelfOf(sam), onePiece1(coverSource = "inventaire.io")).expectStatus().isCreated()

    // When
    val cover = awaitCover(sam, ONE_PIECE_1)

    // Then
    val response = picture(sam, cover).expectStatus().isOk()
    response.expectHeader().contentType(WEBP)
    bytesOf(response) shouldBe SMALL_WEBP
  }

  @Test
  @Disabled("covers")
  fun `S7 An edition without a chosen source gets the cascade, the first source has it`() {
    // Given
    val tom = reader("tom", "Tom")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    bnf.knows(ONE_PIECE_1)
    bnf.hasCover(TALL_JPEG)
    add(tom, defaultBookshelfOf(tom), onePiece1(coverSource = null)).expectStatus().isCreated()

    // When
    val cover = awaitCover(tom, ONE_PIECE_1)

    // Then
    servedAs(tom, cover, WEBP)
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 0
    bnf.coverRequests() shouldBe 0
  }

  @Test
  @Disabled("covers")
  fun `S7 An edition without a chosen source gets the cascade, only the last has it`() {
    // Given
    val zoe = reader("zoe", "Zoé")
    inventaire.doesNotKnow(LES_NERONIA)
    openLibrary.hasNoCover(LES_NERONIA)
    bnf.knows(LES_NERONIA)
    bnf.hasCover(TALL_JPEG)
    add(zoe, defaultBookshelfOf(zoe), lesNeronia(coverSource = null)).expectStatus().isCreated()

    // When
    val cover = awaitCover(zoe, LES_NERONIA)

    // Then
    servedAs(zoe, cover, IMAGE_JPEG)
    openLibrary.coverRequests(LES_NERONIA) shouldBe 1
    bnf.coverRequests() shouldBe 1
  }

  @Test
  @Disabled("covers")
  fun `S7 An edition without a chosen source gets the cascade, none has it`() {
    // Given
    val eve = reader("eve", "Ève")
    inventaire.doesNotKnow(LES_NERONIA)
    openLibrary.hasNoCover(LES_NERONIA)
    bnf.knows(LES_NERONIA)
    bnf.hasNoCover()
    add(eve, defaultBookshelfOf(eve), lesNeronia(coverSource = null)).expectStatus().isCreated()

    // When
    await { bnf.coverRequests() == 1 }

    // Then
    openLibrary.coverRequests(LES_NERONIA) shouldBe 1
    coverOf(eve, LES_NERONIA).shouldBeNull()
  }

  @Test
  @Disabled("covers")
  fun `S8 A failed fetch waits a day, the source does not answer`() {
    openLibrary.coverAnswersTooLate(ONE_PIECE_1)
    aFailedFetchWaitsADay()
  }

  @Test
  @Disabled("covers")
  fun `S8 A failed fetch waits a day, the source answers an error`() {
    openLibrary.coverFails(ONE_PIECE_1)
    aFailedFetchWaitsADay()
  }

  @Test
  @Disabled("covers")
  fun `S8 A failed fetch waits a day, the source answers what is not a picture`() {
    openLibrary.coverIsNotAPicture(ONE_PIECE_1)
    aFailedFetchWaitsADay()
  }

  private fun aFailedFetchWaitsADay() {
    // Given
    val luc = reader("luc", "Luc")
    val bookshelf = defaultBookshelfOf(luc)
    openLibrary.hasCover(ONE_PIECE_2, TALL_JPEG)
    openLibrary.hasCover(ONE_PIECE_3, TALL_JPEG)
    add(luc, bookshelf, onePiece1(coverSource = "Open Library")).expectStatus().isCreated()
    await { openLibrary.coverRequests(ONE_PIECE_1) == 1 }
    coverOf(luc, ONE_PIECE_1).shouldBeNull()

    // When a run within the day, woken by another add
    add(luc, bookshelf, onePiece(ONE_PIECE_2, "Aux prises avec Baggy et ses hommes", 2, "Open Library"))
      .expectStatus()
      .isCreated()
    awaitCover(luc, ONE_PIECE_2)

    // Then
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 1
    coverOf(luc, ONE_PIECE_1).shouldBeNull()

    // When a run a day later
    clock.advance(Duration.ofHours(A_DAY_AND_MORE))
    add(luc, bookshelf, onePiece(ONE_PIECE_3, "Piège", 3, "Open Library")).expectStatus().isCreated()
    awaitCover(luc, ONE_PIECE_3)

    // Then
    await { openLibrary.coverRequests(ONE_PIECE_1) == 2 }
  }

  @Test
  @Disabled("covers")
  fun `S9 The worker runs on its own`() {
    // Given
    val ana = reader("ana", "Ana")
    val bookshelf = defaultBookshelfOf(ana)
    openLibrary.hasCoverAfter(ONE_PIECE_1, TALL_JPEG, SLOWLY)
    openLibrary.hasCover(ONE_PIECE_2, TALL_JPEG)

    // When
    add(ana, bookshelf, onePiece1(coverSource = "Open Library")).expectStatus().isCreated()
    add(ana, bookshelf, onePiece(ONE_PIECE_2, "Aux prises avec Baggy et ses hommes", 2, "Open Library"))
      .expectStatus()
      .isCreated()
    awaitCover(ana, ONE_PIECE_1)
    awaitCover(ana, ONE_PIECE_2)

    // Then
    val first = openLibrary.coverRequestTimes(ONE_PIECE_1).shouldHaveSize(1).single()
    val second = openLibrary.coverRequestTimes(ONE_PIECE_2).shouldHaveSize(1).single()
    second.time shouldBeGreaterThanOrEqualTo first.time + SLOWLY
  }

  @Test
  @Disabled("covers")
  fun `S10 A held edition offers its own cover`() {
    // Given
    val iris = reader("iris", "Iris")
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    add(iris, defaultBookshelfOf(iris), onePiece1(coverSource = "Open Library")).expectStatus().isCreated()
    val cover = awaitCover(iris, ONE_PIECE_1)

    // When
    val response = ask(iris, ONE_PIECE_1)

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
    val hugo = reader("hugo", "Hugo")
    openLibrary.coverAnswersTooLate(ONE_PIECE_1)
    add(hugo, defaultBookshelfOf(hugo), onePiece1(coverSource = "Open Library")).expectStatus().isCreated()

    // When
    val response = ask(hugo, ONE_PIECE_1)

    // Then
    val body = bodyOf(response.expectStatus().isOk())
    JsonPath.read<List<String>>(body, "$.covers[*].source") shouldBe emptyList()
  }

  private fun ask(reader: Map<String, List<String>>, isbn: String): RestTestClient.ResponseSpec =
    http
      .get()
      .uri("/api/v1/isbn/$isbn")
      .headers { it.putAll(reader) }
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .exchange()

  private fun add(reader: Map<String, List<String>>, bookshelf: String, book: String): RestTestClient.ResponseSpec =
    http
      .post()
      .uri("/api/v1/bookshelves/$bookshelf/books")
      .headers { it.putAll(reader) }
      .contentType(APPLICATION_JSON)
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .body(book)
      .exchange()

  private fun catalogue(reader: Map<String, List<String>>): RestTestClient.ResponseSpec =
    http
      .get()
      .uri("/api/v1/books")
      .headers { it.putAll(reader) }
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .exchange()

  private fun picture(reader: Map<String, List<String>>, path: String): RestTestClient.ResponseSpec =
    http
      .get()
      .uri(path)
      .headers { it.putAll(reader) }
      .accept(parseMediaType("image/*"), APPLICATION_PROBLEM_JSON)
      .exchange()

  private fun servedAs(reader: Map<String, List<String>>, cover: String, type: MediaType) {
    picture(reader, cover).expectStatus().isOk().expectHeader().contentType(type)
  }

  private fun me(reader: Map<String, List<String>>): RestTestClient.ResponseSpec =
    http
      .get()
      .uri("/api/v1/me")
      .headers { it.putAll(reader) }
      .accept(APPLICATION_JSON)
      .exchange()

  private fun defaultBookshelfOf(reader: Map<String, List<String>>): String =
    JsonPath.read(bodyOf(me(reader).expectStatus().isOk()), "$.defaultBookshelf.id")

  private fun coverOf(reader: Map<String, List<String>>, isbn: String): String? =
    JsonPath.read<List<String?>>(
      bodyOf(catalogue(reader).expectStatus().isOk()),
      "$.books[?(@.isbn13 == '$isbn')].coverUrl",
    ).single()

  private fun awaitCover(reader: Map<String, List<String>>, isbn: String): String {
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

  private fun bodyOf(response: RestTestClient.ResponseSpec): String =
    requireNotNull(response.expectBody(String::class.java).returnResult().responseBody)

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

  private fun reader(username: String, name: String) =
    mapOf(
      "Remote-User" to listOf(username),
      "Remote-Name" to listOf(name),
      "Remote-Email" to listOf("$username@amory.fr"),
      "Remote-Groups" to listOf("family"),
    )

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
