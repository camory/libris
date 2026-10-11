package fr.amory.libris.scenario

import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.CONFLICT
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NOT_FOUND
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.Role
import fr.amory.libris.scenario.dsl.Role.OWNER
import fr.amory.libris.scenario.dsl.Role.VIEWER
import fr.amory.libris.scenario.dsl.Series
import fr.amory.libris.scenario.dsl.adds
import fr.amory.libris.scenario.dsl.asks
import fr.amory.libris.scenario.dsl.bookshelves
import fr.amory.libris.scenario.dsl.catalogue
import fr.amory.libris.scenario.dsl.creates
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.hasCreated
import fr.amory.libris.scenario.dsl.invalid
import fr.amory.libris.scenario.dsl.makesDefault
import fr.amory.libris.scenario.dsl.member
import fr.amory.libris.scenario.dsl.opens
import fr.amory.libris.scenario.dsl.readers
import fr.amory.libris.scenario.dsl.withRole
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.FieldSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.client.RestTestClient

@ScenarioTest
class BookshelvesScenarios @Autowired constructor(http: RestTestClient) {
  private val libris = Libris(http)

  @Test
  @Disabled("bookshelves")
  fun `S1 The reader's bookshelves`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val marc = libris.reader("marc", "Marc")
    val default = lea.defaultBookshelf
    val aLire = lea.hasCreated("À lire")
    val bandesDessinees = marc.hasCreated("Bandes dessinées")
    marc.hasAdded(lea, VIEWER, to = bandesDessinees)

    // When
    val bookshelves = lea.bookshelves()

    // Then
    bookshelves shouldBe listOf(default.withRole(OWNER), aLire.withRole(OWNER), bandesDessinees.withRole(VIEWER))
    default.name shouldBe "Bibliothèque de Léa"
  }

  @Test
  @Disabled("bookshelves")
  fun `S2 A reader creates a bookshelf`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val default = lea.defaultBookshelf

    // When
    val answer = lea.creates("Bibliothèque familiale")

    // Then
    val familiale = answer.value
    familiale.name shouldBe "Bibliothèque familiale"
    lea.bookshelves() shouldContainExactlyInAnyOrder listOf(default.withRole(OWNER), familiale.withRole(OWNER))
    lea.defaultBookshelf shouldBe default
  }

  @Test
  @Disabled("bookshelves")
  fun `S3 A name Libris refuses, a blank name`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val default = lea.defaultBookshelf

    // When
    val answer = lea.creates("   ")

    // Then
    answer.refusal shouldBe invalid("name")
    lea.bookshelves() shouldBe listOf(default.withRole(OWNER))
  }

  @Test
  @Disabled("bookshelves")
  fun `S3 A name Libris refuses, a name taken`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val marc = libris.reader("marc", "Marc")
    val default = lea.defaultBookshelf
    val familiale = marc.hasCreated("Bibliothèque familiale")
    marc.hasAdded(lea, VIEWER, to = familiale)

    // When
    val answer = lea.creates("bibliotheque FAMILIALE")

    // Then
    answer.refusal shouldBe CONFLICT
    lea.bookshelves() shouldContainExactlyInAnyOrder listOf(default.withRole(OWNER), familiale.withRole(VIEWER))
  }

  @Test
  @Disabled("bookshelves")
  fun `S4 A member opens a bookshelf`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    val lea = libris.reader("lea", "Léa")
    val elise = libris.reader("elise", "Élise")
    val familiale = marc.hasCreated("Bibliothèque familiale")
    marc.hasAdded(lea, VIEWER, to = familiale)
    marc.hasAdded(elise, OWNER, to = familiale)

    // When
    val answer = lea.opens(familiale)

    // Then
    answer.value.name shouldBe "Bibliothèque familiale"
    answer.value.members shouldBe listOf(elise.member(OWNER), marc.member(OWNER), lea.member(VIEWER))
  }

  @Test
  @Disabled("bookshelves")
  fun `S4 A member opens a bookshelf, a reader who does not belong to it`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    val lea = libris.reader("lea", "Léa")
    val familiale = marc.hasCreated("Bibliothèque familiale")

    // When
    val answer = lea.opens(familiale)

    // Then
    answer.refusal shouldBe NOT_FOUND
  }

  @ParameterizedTest(name = "S5 An owner adds a member, {argumentSetName}")
  @FieldSource("ADDED_AS")
  @Disabled("bookshelves")
  fun `S5 An owner adds a member`(role: Role) {
    // Given
    val marc = libris.reader("marc", "Marc")
    val lea = libris.knownReader("lea", "Léa")
    val familiale = marc.hasCreated("Bibliothèque familiale")
    val copy = marc.hasAdded(ONE_PIECE_1, to = familiale)

    // When
    val found = marc.readers().single { it.displayName == "Léa" }
    val answer = marc.adds(found, role, to = familiale)

    // Then
    answer.value shouldBe lea.member(role)
    lea.bookshelves() shouldContain familiale.withRole(role)
    lea.catalogue().books.single { it.isbn13 == ONE_PIECE_1.isbn13 }.copies shouldBe listOf(copy)
    lea.asks(ONE_PIECE_1.isbn13).value.copies shouldBe listOf(copy)
  }

  @Test
  @Disabled("bookshelves")
  fun `S6 An add Libris refuses, a viewer of the bookshelf adds a reader`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    val lea = libris.reader("lea", "Léa")
    val sam = libris.knownReader("sam", "Sam")
    val familiale = marc.hasCreated("Bibliothèque familiale")
    marc.hasAdded(lea, VIEWER, to = familiale)

    // When
    val answer = lea.adds(sam, VIEWER, to = familiale)

    // Then
    answer.refusal shouldBe NOT_FOUND
    marc.opens(familiale).value.members shouldBe listOf(marc.member(OWNER), lea.member(VIEWER))
  }

  @Test
  @Disabled("bookshelves")
  fun `S6 An add Libris refuses, a reader who does not belong to it adds a reader`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    val lea = libris.reader("lea", "Léa")
    val sam = libris.knownReader("sam", "Sam")
    val familiale = marc.hasCreated("Bibliothèque familiale")

    // When
    val answer = lea.adds(sam, VIEWER, to = familiale)

    // Then
    answer.refusal shouldBe NOT_FOUND
    marc.opens(familiale).value.members shouldBe listOf(marc.member(OWNER))
  }

  @Test
  @Disabled("bookshelves")
  fun `S6 An add Libris refuses, an owner adds a reader Libris does not know`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    val stranger = libris.unknownReader()
    val familiale = marc.hasCreated("Bibliothèque familiale")

    // When
    val answer = marc.adds(stranger, VIEWER, to = familiale)

    // Then
    answer.refusal shouldBe invalid("readerId")
    marc.opens(familiale).value.members shouldBe listOf(marc.member(OWNER))
  }

  @Test
  @Disabled("bookshelves")
  fun `S6 An add Libris refuses, an owner adds a reader already a member`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    val lea = libris.reader("lea", "Léa")
    val familiale = marc.hasCreated("Bibliothèque familiale")
    marc.hasAdded(lea, VIEWER, to = familiale)

    // When
    val answer = marc.adds(lea, OWNER, to = familiale)

    // Then
    answer.refusal shouldBe CONFLICT
    marc.opens(familiale).value.members shouldBe listOf(marc.member(OWNER), lea.member(VIEWER))
  }

  @Test
  @Disabled("bookshelves")
  fun `S7 A reader chooses their default`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val first = lea.defaultBookshelf
    val salon = lea.hasCreated("Salon")

    // When
    val answer = lea.makesDefault(salon)

    // Then
    answer.refusal shouldBe null
    lea.defaultBookshelf shouldBe salon
    lea.bookshelves() shouldBe listOf(salon.withRole(OWNER), first.withRole(OWNER))
  }

  @Test
  @Disabled("bookshelves")
  fun `S8 A bookshelf the reader cannot make their default, a bookshelf they are a viewer of`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val marc = libris.reader("marc", "Marc")
    val default = lea.defaultBookshelf
    val salon = marc.hasCreated("Salon")
    marc.hasAdded(lea, VIEWER, to = salon)

    // When
    val answer = lea.makesDefault(salon)

    // Then
    answer.refusal shouldBe NOT_FOUND
    lea.defaultBookshelf shouldBe default
  }

  @Test
  @Disabled("bookshelves")
  fun `S8 A bookshelf the reader cannot make their default, one they do not belong to`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val marc = libris.reader("marc", "Marc")
    val default = lea.defaultBookshelf
    val salon = marc.hasCreated("Salon")

    // When
    val answer = lea.makesDefault(salon)

    // Then
    answer.refusal shouldBe NOT_FOUND
    lea.defaultBookshelf shouldBe default
  }

  private companion object {
    @JvmField
    val ADDED_AS = listOf(argumentSet("added as owner", OWNER), argumentSet("added as viewer", VIEWER))

    val ONE_PIECE_1 = NewBook(
      isbn13 = "9782723488525",
      kind = "MANGA",
      title = "Romance dawn",
      authors = listOf(Author("Eiichirō Oda", "WRITER")),
      series = Series("One piece", 1),
      publisher = "Glénat",
    )
  }
}
