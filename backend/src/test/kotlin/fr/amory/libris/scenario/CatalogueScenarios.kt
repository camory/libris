package fr.amory.libris.scenario

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.Optional

@ScenarioTest
class CatalogueScenarios @Autowired constructor(
    private val http: RestTestClient,
) {
    @Test
    @Disabled("T044")
    fun `S1 The catalogue lists the house's editions`() {
        // Given
        val lea = reader("lea", "Léa")
        val sam = reader("sam", "Sam")
        val leasBookshelf = defaultBookshelfOf(lea)
        listOf(ONE_PIECE_2, EMILE, ASTERIX_ET_SES_AMIS, ONE_PIECE_1, ONE_PIECE_1, LA_SERPE_D_OR, ASTERIX_LE_GAULOIS)
            .forEach { add(lea, leasBookshelf, it).expectStatus().isCreated() }
        add(sam, defaultBookshelfOf(sam), ONE_PIECE_3).expectStatus().isCreated()
        // When
        val response = catalogue(lea)
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
    @Disabled("T045")
    fun `S2 The catalogue comes in pages`() {
        // Given
        val marc = reader("marc", "Marc")
        val bookshelf = defaultBookshelfOf(marc)
        (1..51).forEach { add(marc, bookshelf, roman(it)).expectStatus().isCreated() }
        // When
        val firstPage = catalogue(marc)
        // Then
        firstPage.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.books.length()").isEqualTo(50)
            .jsonPath("$.books[0].title").isEqualTo("Roman 01")
            .jsonPath("$.books[49].title").isEqualTo("Roman 50")
            .jsonPath("$.next").isNotEmpty()
        val next: String = JsonPath.read(bodyOf(firstPage), "$.next")
        catalogue(marc, after = next).expectStatus().isOk()
            .expectBody()
            .jsonPath("$.books[*].title").isEqualTo(listOf("Roman 51"))
            .jsonPath("$.next").isEqualTo(null)
    }

    @Test
    fun `S3 The catalogue is empty`() {
        // Given
        val nina = reader("nina", "Nina")
        // When
        val response = catalogue(nina)
        // Then
        response.expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.books").isEmpty()
            .jsonPath("$.next").isEqualTo(null)
    }

    private fun catalogue(reader: Map<String, List<String>>, after: String? = null): RestTestClient.ResponseSpec =
        http.get()
            .uri { it.path("/api/v1/books").queryParamIfPresent("after", Optional.ofNullable(after)).build() }
            .headers { it.putAll(reader) }
            .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
            .exchange()

    private fun me(reader: Map<String, List<String>>): RestTestClient.ResponseSpec = http.get()
        .uri("/api/v1/me")
        .headers { it.putAll(reader) }
        .accept(APPLICATION_JSON)
        .exchange()

    private fun add(reader: Map<String, List<String>>, bookshelf: String, book: String): RestTestClient.ResponseSpec =
        http.post()
            .uri("/api/v1/bookshelves/$bookshelf/books")
            .headers { it.putAll(reader) }
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
            .body(book)
            .exchange()

    private fun defaultBookshelfOf(reader: Map<String, List<String>>): String =
        JsonPath.read(bodyOf(me(reader).expectStatus().isOk()), "$.defaultBookshelf.id")

    private fun bodyOf(response: RestTestClient.ResponseSpec): String =
        requireNotNull(response.expectBody(String::class.java).returnResult().responseBody)

    private fun reader(username: String, name: String) = mapOf(
        "Remote-User" to listOf(username),
        "Remote-Name" to listOf(name),
        "Remote-Email" to listOf("$username@amory.fr"),
        "Remote-Groups" to listOf("family"),
    )

    private companion object {
        val ONE_PIECE_1 = manga("9782723488525", "Romance dawn", "One piece", 1)
        val ONE_PIECE_2 = manga("9782723489898", "Aux prises avec Baggy et ses hommes", "One piece", 2)
        val ONE_PIECE_3 = manga("9782723489904", "Piège", "One piece", 3)
        val ASTERIX_LE_GAULOIS = bd("9782012101333", "Astérix le Gaulois", "Astérix", 1)
        val LA_SERPE_D_OR = bd("9782012101340", "La serpe d'or", "Astérix", 2)
        val ASTERIX_ET_SES_AMIS = bd("9782226174208", "Astérix et ses amis", "Astérix", null)
        val EMILE = book("9782013224895", "Émile et les détectives")

        fun manga(isbn: String, title: String, series: String, volume: Int) = edition(
            isbn = isbn,
            kind = "MANGA",
            title = title,
            authors = """
                [{ "name": "Eiichirō Oda", "role": "WRITER" }, { "name": "Eiichirō Oda", "role": "ARTIST" }]
            """.trimIndent(),
            series = """{ "name": "$series", "volumeNumber": $volume }""",
        )

        fun bd(isbn: String, title: String, series: String, volume: Int?) = edition(
            isbn = isbn,
            kind = "BD",
            title = title,
            authors = """
                [{ "name": "René Goscinny", "role": "WRITER" }, { "name": "Albert Uderzo", "role": "ARTIST" }]
            """.trimIndent(),
            series = """{ "name": "$series", "volumeNumber": $volume }""",
        )

        fun book(isbn: String, title: String) = edition(
            isbn = isbn,
            kind = "BOOK",
            title = title,
            authors = """[{ "name": "Erich Kästner", "role": "WRITER" }]""",
            series = "null",
        )

        fun roman(number: Int) = edition(
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
