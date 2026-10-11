package fr.amory.libris.scenario

import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.InventaireStubs
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.bibliography.fixture.recordedBytes
import fr.amory.libris.fixture.MutableClock
import fr.amory.libris.scenario.dsl.Answer
import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Copy
import fr.amory.libris.scenario.dsl.Cover
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NOT_FOUND
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.Reader
import fr.amory.libris.scenario.dsl.Series
import fr.amory.libris.scenario.dsl.Sources
import fr.amory.libris.scenario.dsl.adds
import fr.amory.libris.scenario.dsl.asks
import fr.amory.libris.scenario.dsl.await
import fr.amory.libris.scenario.dsl.awaitsCoverOf
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.seesCoverOf
import fr.amory.libris.scenario.dsl.views
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
import org.springframework.http.MediaType
import org.springframework.http.MediaType.IMAGE_JPEG
import org.springframework.http.MediaType.parseMediaType
import java.io.ByteArrayInputStream
import java.time.Duration
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@ScenarioTest
class CoversScenarios @Autowired constructor(
  private val libris: Libris,
  private val clock: MutableClock,
  private val bnf: BnfStubs,
  private val openLibrary: OpenLibraryStubs,
  private val inventaire: InventaireStubs,
  private val sources: Sources) {

  @Test
  fun `S1 The lookup offers the sources' covers`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    bnf.knows(ONE_PIECE_1)
    openLibrary.knows(ONE_PIECE_1)
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)

    // When
    val answer = lea.asks(ONE_PIECE_1)

    // Then
    val card = answer.value
    card.covers.map { it.source } shouldBe listOf("inventaire.io", "Open Library", "BnF")
    card.id shouldBe null
    card.covers[0].url shouldContain ONE_PIECE_1_INVENTAIRE_HASH
    card.covers[1].url shouldContain ONE_PIECE_1
    card.covers[2].url shouldContain ONE_PIECE_1_ARK
    noPictureWasFetched()
  }

  @Test
  fun `S3 The add carries the cover's source`() {
    // Given
    val juliette = libris.reader("juliette", "Juliette")
    val bookshelf = juliette.defaultBookshelf
    inventaire.knowsButThePictureNeverComes(ONE_PIECE_1)

    // When
    val answer: Answer<Copy>
    val elapsed = measureTime { answer = juliette.adds(onePiece1(coverSource = "inventaire.io"), to = bookshelf) }

    // Then
    answer.refusal shouldBe null
    elapsed shouldBeLessThan 2.seconds
    juliette.seesCoverOf(ONE_PIECE_1).shouldBeNull()
  }

  @Test
  fun `S4 The worker fetches the chosen cover`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    bnf.knows(ONE_PIECE_1)
    bnf.hasCover(TALL_JPEG)
    marc.hasAdded(onePiece1(coverSource = "inventaire.io"), to = marc.defaultBookshelf)

    // When
    val cover = marc.awaitsCoverOf(ONE_PIECE_1)

    // Then
    cover shouldStartWith "/api/v1/covers/"
    marc.views(cover).value.format shouldBe WEBP
    inventaire.pictureRequests() shouldBe 1
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 0
    bnf.coverRequests() shouldBe 0
  }

  @Test
  fun `S5 Libris serves a stored cover`() {
    // Given
    val nina = libris.reader("nina", "Nina")
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    nina.hasAdded(onePiece1(coverSource = "Open Library"), to = nina.defaultBookshelf)
    val cover = nina.awaitsCoverOf(ONE_PIECE_1)

    // When
    val answer = nina.views(cover)

    // Then
    val picture = answer.value
    picture.format shouldBe IMAGE_JPEG
    picture.keptFor shouldBe 365.days
    picture.immutable shouldBe true
  }

  @Test
  fun `S5 Libris serves a stored cover, an address naming no cover`() {
    // Given
    val paul = libris.reader("paul", "Paul")

    // When
    val answer = paul.views("/api/v1/covers/${"0".repeat(HASH_LENGTH)}")

    // Then
    answer.refusal shouldBe NOT_FOUND
  }

  @Test
  fun `S6 The picture is normalised, taller than 600`() {
    // Given
    val rose = libris.reader("rose", "Rose")
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    rose.hasAdded(onePiece1(coverSource = "Open Library"), to = rose.defaultBookshelf)

    // When
    val cover = rose.awaitsCoverOf(ONE_PIECE_1)

    // Then
    val picture = rose.views(cover).value
    picture.format shouldBe IMAGE_JPEG
    val stored = ImageIO.read(ByteArrayInputStream(picture.bytes))
    stored.height shouldBe NORMALISED_HEIGHT
    stored.width shouldBe NORMALISED_HEIGHT * TALL_JPEG_WIDTH / TALL_JPEG_HEIGHT
  }

  @Test
  fun `S6 The picture is normalised, 600 tall or less`() {
    // Given
    val sam = libris.reader("sam", "Sam")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    sam.hasAdded(onePiece1(coverSource = "inventaire.io"), to = sam.defaultBookshelf)

    // When
    val cover = sam.awaitsCoverOf(ONE_PIECE_1)

    // Then
    val picture = sam.views(cover).value
    picture.format shouldBe WEBP
    picture.bytes shouldBe SMALL_WEBP
  }

  @Test
  fun `S7 An edition without a chosen source gets the cascade, the first source has it`() {
    // Given
    val tom = libris.reader("tom", "Tom")
    inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
    openLibrary.hasCover(ONE_PIECE_1, TALL_JPEG)
    bnf.knows(ONE_PIECE_1)
    bnf.hasCover(TALL_JPEG)
    tom.hasAdded(onePiece1(coverSource = null), to = tom.defaultBookshelf)

    // When
    val cover = tom.awaitsCoverOf(ONE_PIECE_1)

    // Then
    tom.views(cover).value.format shouldBe WEBP
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 0
    bnf.coverRequests() shouldBe 0
  }

  @Test
  fun `S7 An edition without a chosen source gets the cascade, only the last has it`() {
    // Given
    val zoe = libris.reader("zoe", "Zoé")
    inventaire.doesNotKnow(LES_NERONIA)
    openLibrary.hasNoCover(LES_NERONIA)
    bnf.knows(LES_NERONIA)
    bnf.hasCover(TALL_JPEG)
    zoe.hasAdded(lesNeronia(coverSource = null), to = zoe.defaultBookshelf)

    // When
    val cover = zoe.awaitsCoverOf(LES_NERONIA)

    // Then
    zoe.views(cover).value.format shouldBe IMAGE_JPEG
    openLibrary.coverRequests(LES_NERONIA) shouldBe 1
    bnf.coverRequests() shouldBe 1
  }

  @Test
  fun `S7 An edition without a chosen source gets the cascade, none has it`() {
    // Given
    val eve = libris.reader("eve", "Ève")
    inventaire.doesNotKnow(LES_NERONIA)
    openLibrary.hasNoCover(LES_NERONIA)
    bnf.knows(LES_NERONIA)
    bnf.hasNoCover()
    eve.hasAdded(lesNeronia(coverSource = null), to = eve.defaultBookshelf)

    // When
    await { bnf.coverRequests() == 1 }

    // Then
    openLibrary.coverRequests(LES_NERONIA) shouldBe 1
    eve.seesCoverOf(LES_NERONIA).shouldBeNull()
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
    val luc = libris.reader("luc", "Luc")
    val bookshelf = luc.defaultBookshelf
    openLibrary.hasCover(ONE_PIECE_2, TALL_JPEG)
    openLibrary.hasCover(ONE_PIECE_3, TALL_JPEG)
    luc.hasAdded(onePiece1(coverSource = "Open Library"), to = bookshelf)
    await { openLibrary.coverRequests(ONE_PIECE_1) == 1 }
    luc.seesCoverOf(ONE_PIECE_1).shouldBeNull()

    // When a run within the day, woken by another add
    luc.hasAdded(onePiece(ONE_PIECE_2, "Aux prises avec Baggy et ses hommes", 2, "Open Library"), to = bookshelf)
    luc.awaitsCoverOf(ONE_PIECE_2)

    // Then
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 1
    luc.seesCoverOf(ONE_PIECE_1).shouldBeNull()

    // When a run a day later
    clock.advance(Duration.ofHours(A_DAY_AND_MORE))
    luc.hasAdded(onePiece(ONE_PIECE_3, "Piège", 3, "Open Library"), to = bookshelf)
    luc.awaitsCoverOf(ONE_PIECE_3)

    // Then
    await { openLibrary.coverRequests(ONE_PIECE_1) == 2 }
  }

  @Test
  fun `S9 The worker runs on its own`() {
    // Given
    val ana = libris.reader("ana", "Ana")
    val bookshelf = ana.defaultBookshelf
    openLibrary.hasCoverAfter(ONE_PIECE_1, TALL_JPEG, SLOWLY)
    openLibrary.hasCover(ONE_PIECE_2, TALL_JPEG)

    // When
    ana.hasAdded(onePiece1(coverSource = "Open Library"), to = bookshelf)
    ana.hasAdded(onePiece(ONE_PIECE_2, "Aux prises avec Baggy et ses hommes", 2, "Open Library"), to = bookshelf)
    ana.awaitsCoverOf(ONE_PIECE_1)
    ana.awaitsCoverOf(ONE_PIECE_2)

    // Then
    val first = openLibrary.coverRequestTimes(ONE_PIECE_1).shouldHaveSize(1).single()
    val second = openLibrary.coverRequestTimes(ONE_PIECE_2).shouldHaveSize(1).single()
    second.time shouldBeGreaterThanOrEqualTo first.time + SLOWLY
  }

  @Test
  fun `S10 A held edition offers its own cover`() {
    // Given
    val iris = libris.reader("iris", "Iris")
    val cover = aStoredCover(iris, ONE_PIECE_1)

    // When
    val answer = iris.asks(ONE_PIECE_1)

    // Then
    val card = answer.value
    card.covers shouldBe listOf(Cover("Libris", cover))
    card.id shouldNotBe null
    sources.wereNotAsked()
  }

  @Test
  fun `S10 A held edition offers its own cover, not yet stored`() {
    // Given
    val hugo = libris.reader("hugo", "Hugo")
    openLibrary.coverAnswersTooLate(ONE_PIECE_1)
    hugo.hasAdded(onePiece1(coverSource = "Open Library"), to = hugo.defaultBookshelf)

    // When
    val answer = hugo.asks(ONE_PIECE_1)

    // Then
    answer.value.covers shouldBe emptyList()
  }

  private fun aStoredCover(reader: Reader, isbn: String): String {
    openLibrary.hasCover(isbn, TALL_JPEG)
    reader.hasAdded(onePiece(isbn, "Romance dawn", 1, "Open Library"), to = reader.defaultBookshelf)
    val cover = reader.awaitsCoverOf(isbn)
    sources.forgetRequests()
    return cover
  }

  private fun noPictureWasFetched() {
    inventaire.pictureRequests() shouldBe 0
    openLibrary.coverRequests(ONE_PIECE_1) shouldBe 0
    bnf.coverRequests() shouldBe 0
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
    const val A_DAY_AND_MORE = 25L
    val WEBP: MediaType = parseMediaType("image/webp")
    val TALL_JPEG = recordedBytes("covers/tall.jpg")
    val SMALL_WEBP = recordedBytes("covers/small.webp")
    val HUGE_HEADER_JPEG = recordedBytes("covers/huge-header.jpg")

    fun onePiece1(coverSource: String?) =
      onePiece(ONE_PIECE_1, "Romance dawn", 1, coverSource)

    fun onePiece(isbn: String, title: String, volume: Int, coverSource: String?) =
      NewBook(
        isbn13 = isbn,
        kind = "MANGA",
        title = title,
        authors = listOf(Author("Eiichirō Oda", "WRITER"), Author("Eiichirō Oda", "ARTIST")),
        series = Series("One piece", volume),
        language = "fr",
        coverSource = coverSource,
      )

    fun lesNeronia(coverSource: String?) =
      NewBook(
        isbn13 = LES_NERONIA,
        kind = "BD",
        title = "Les Neronia",
        authors = listOf(Author("Jean Dufaux", "WRITER"), Author("Jérémy", "ARTIST")),
        language = "fr",
        coverSource = coverSource,
      )
  }
}
