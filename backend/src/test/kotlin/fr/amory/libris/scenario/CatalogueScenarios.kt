package fr.amory.libris.scenario

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.client.RestTestClient

@ScenarioTest
class CatalogueScenarios @Autowired constructor(
  http: RestTestClient) {
  private val libris = Libris(http)

  @Test
  fun `S1 The catalogue lists the house's editions`() {
    // Given
    val lea = Reader("lea", "Léa")
    val sam = Reader("sam", "Sam")
    val leasBookshelf = libris.defaultBookshelfOf(lea)
    listOf(ONE_PIECE_2, EMILE, ASTERIX_ET_SES_AMIS, ONE_PIECE_1, ONE_PIECE_1, LA_SERPE_D_OR, ASTERIX_LE_GAULOIS)
      .forEach { libris.add(lea, leasBookshelf, it).expectStatus().isCreated() }
    libris.add(sam, libris.defaultBookshelfOf(sam), ONE_PIECE_3).expectStatus().isCreated()
    // When
    val response = libris.catalogue(lea)
    // Then
    response.expectStatus().isOk()
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.books[*].title").isEqualTo(
        listOf(
          "Astérix le Gaulois",
          "La serpe d'or",
          "Astérix et ses amis",
          "Émile et les détectives",
          "Romance dawn",
          "Aux prises avec Baggy et ses hommes",
        ),
      )
      .jsonPath("$.books[0].series.name").isEqualTo("Astérix")
      .jsonPath("$.books[0].series.volumeNumber").isEqualTo(1)
      .jsonPath("$.books[2].series.volumeNumber").isEqualTo(null)
      .jsonPath("$.books[3].series").isEqualTo(null)
      .jsonPath("$.books[4].copies.length()").isEqualTo(2)
      .jsonPath("$.books[4].copies[*].bookshelf.name")
      .isEqualTo(listOf("Bibliothèque de Léa", "Bibliothèque de Léa"))
      .jsonPath("$.books[*].copies[*].bookshelf.id").isEqualTo(List(7) { leasBookshelf })
      .jsonPath("$.next").isEqualTo(null)
  }

  @Test
  fun `S2 The catalogue comes in pages`() {
    // Given
    val marc = Reader("marc", "Marc")
    val bookshelf = libris.defaultBookshelfOf(marc)
    (1..51).forEach { libris.add(marc, bookshelf, roman(it)).expectStatus().isCreated() }
    // When
    val firstPage = libris.catalogue(marc)
    // Then
    firstPage.expectStatus().isOk()
      .expectBody()
      .jsonPath("$.books.length()").isEqualTo(50)
      .jsonPath("$.books[0].title").isEqualTo("Roman 01")
      .jsonPath("$.books[49].title").isEqualTo("Roman 50")
      .jsonPath("$.next").isNotEmpty()
    val next: String = JsonPath.read(bodyOf(firstPage), "$.next")
    libris.catalogue(marc, after = next).expectStatus().isOk()
      .expectBody()
      .jsonPath("$.books[*].title").isEqualTo(listOf("Roman 51"))
      .jsonPath("$.next").isEqualTo(null)
  }

  @Test
  fun `S3 The catalogue is empty`() {
    // Given
    val nina = Reader("nina", "Nina")
    // When
    val response = libris.catalogue(nina)
    // Then
    response.expectStatus().isOk()
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.books").isEmpty()
      .jsonPath("$.next").isEqualTo(null)
  }

  private companion object {
    val ONE_PIECE_1 = manga("9782723488525", "Romance dawn", "One piece", 1)
    val ONE_PIECE_2 = manga("9782723489898", "Aux prises avec Baggy et ses hommes", "One piece", 2)
    val ONE_PIECE_3 = manga("9782723489904", "Piège", "One piece", 3)
    val ASTERIX_LE_GAULOIS = bd("9782012101333", "Astérix le Gaulois", "Astérix", 1)
    val LA_SERPE_D_OR = bd("9782012101340", "La serpe d'or", "Astérix", 2)
    val ASTERIX_ET_SES_AMIS = bd("9782226174208", "Astérix et ses amis", "Astérix", null)
    val EMILE = book("9782013224895", "Émile et les détectives")

    fun manga(isbn: String, title: String, series: String, volume: Int) =
      edition(
        isbn = isbn,
        kind = "MANGA",
        title = title,
        authors = """
                [{ "name": "Eiichirō Oda", "role": "WRITER" }, { "name": "Eiichirō Oda", "role": "ARTIST" }]
        """.trimIndent(),
        series = """{ "name": "$series", "volumeNumber": $volume }""",
      )

    fun bd(isbn: String, title: String, series: String, volume: Int?) =
      edition(
        isbn = isbn,
        kind = "BD",
        title = title,
        authors = """
                [{ "name": "René Goscinny", "role": "WRITER" }, { "name": "Albert Uderzo", "role": "ARTIST" }]
        """.trimIndent(),
        series = """{ "name": "$series", "volumeNumber": $volume }""",
      )

    fun book(isbn: String, title: String) =
      edition(
        isbn = isbn,
        kind = "BOOK",
        title = title,
        authors = """[{ "name": "Erich Kästner", "role": "WRITER" }]""",
        series = "null",
      )

    fun roman(number: Int) =
      edition(
        isbn = isbn(number),
        kind = "BOOK",
        title = "Roman " + number.toString().padStart(2, '0'),
        authors = """[{ "name": "Anonyme", "role": "WRITER" }]""",
        series = "null",
      )

    fun isbn(number: Int): String {
      val body = "978200" + number.toString().padStart(6, '0')
      val sum = body.withIndex().sumOf { (i, c) -> c.digitToInt() * if (i % 2 == 0) 1 else 3 }
      return body + (10 - sum % 10) % 10
    }

    fun edition(isbn: String, kind: String, title: String, authors: String, series: String) =
      """
            {
              "isbn13": "$isbn",
              "kind": "$kind",
              "title": "$title",
              "subtitle": null,
              "authors": $authors,
              "series": $series,
              "collection": null,
              "publisher": null,
              "publicationYear": null,
              "language": "fr",
              "pageCount": null,
              "summary": null,
              "coverUrl": null
            }
      """.trimIndent()
  }
}
