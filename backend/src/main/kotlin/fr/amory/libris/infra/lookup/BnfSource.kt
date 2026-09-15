package fr.amory.libris.infra.lookup

import fr.amory.libris.domain.AuthorRole
import fr.amory.libris.domain.AuthorRole.ARTIST
import fr.amory.libris.domain.AuthorRole.TRANSLATOR
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source.BNF
import fr.amory.libris.domain.lookup.SourceAnswer
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.domain.lookup.SourceSeries
import org.springframework.web.client.RestClientException
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.StringReader
import java.time.Duration
import javax.xml.parsers.DocumentBuilderFactory

private const val MARCXCHANGE = "info:lc/xmlns/marcxchange-v2"
private val AUTHOR_TAGS = setOf("700", "701", "702")
private val ROLES = mapOf("070" to WRITER, "440" to ARTIST, "730" to TRANSLATOR)
private val LANGUAGES = mapOf("fre" to "fr")
private val YEAR = Regex("\\d{4}")
private val PAGES = Regex("(\\d+)\\s*p\\.")
private const val ARK = "ark:/"
private const val COVER_BEFORE = "https://catalogue.bnf.fr/couverture?&appName=NE&idArk="
private const val COVER_AFTER = "&couverture=1"

internal fun authorRoleOf(functionCode: String?): AuthorRole = ROLES[functionCode] ?: WRITER

internal fun coverUrlOf(controlField: String?): String? =
    controlField?.indexOf(ARK)?.takeIf { it >= 0 }
        ?.let { COVER_BEFORE + controlField.substring(it) + COVER_AFTER }

class BnfSource(baseUrl: String, timeout: Duration) : IsbnSource {
    override val source = BNF

    private val http = sourceRestClient(baseUrl, timeout)

    override fun lookUp(isbn: Isbn13): SourceAnswer =
        try {
            answerFor(isbn)
        } catch (ignored: RestClientException) {
            Failed
        } catch (ignored: SAXException) {
            Failed
        }

    private fun answerFor(isbn: Isbn13): SourceAnswer =
        recordIn(search(isbn))?.let { answerFrom(isbn, it) } ?: NothingKnown

    private fun answerFrom(isbn: Isbn13, record: UnimarcRecord): SourceAnswer =
        record.value("200", "a")?.let { Known(editionOf(isbn, it, record)) } ?: Failed

    private fun editionOf(isbn: Isbn13, title: String, record: UnimarcRecord): SourceEdition =
        SourceEdition(
            isbn13 = isbn,
            title = title,
            subtitle = record.value("200", "e"),
            authors = authorsOf(record),
            series = seriesOf(record),
            collection = record.value("410", "t"),
            publisher = record.value("210", "c"),
            publicationYear = YEAR.find(record.value("210", "d").orEmpty())?.value?.toIntOrNull(),
            language = LANGUAGES[record.value("101", "a")],
            pageCount = PAGES.find(record.value("215", "a").orEmpty())?.groupValues?.get(1)?.toIntOrNull(),
            summary = null,
            coverUrl = coverUrlOf(record.control("003")),
        )

    private fun authorsOf(record: UnimarcRecord): List<SourceAuthor> =
        record.fields(AUTHOR_TAGS).mapNotNull { field ->
            field.value("a")?.let { surname ->
                val forename = field.value("b")
                SourceAuthor(
                    name = if (forename == null) surname else "$forename $surname",
                    role = authorRoleOf(field.value("4")),
                )
            }
        }

    private fun seriesOf(record: UnimarcRecord): SourceSeries? =
        record.value("461", "t")?.let { SourceSeries(it, record.value("461", "v")?.toIntOrNull()) }

    private fun search(isbn: Isbn13): String = http.get()
        .uri { uri ->
            uri.queryParam("version", "1.2")
                .queryParam("operation", "searchRetrieve")
                .queryParam("recordSchema", "unimarcxchange")
                .queryParam("maximumRecords", "1")
                .queryParam("query", queryFor(isbn))
                .build()
        }
        .retrieve()
        .body(String::class.java)
        .orEmpty()

    private companion object {
        fun queryFor(isbn: Isbn13): String =
            listOfNotNull(isbn.digits, isbn.isbn10).joinToString(" or ") { """bib.isbn all "$it"""" }

        fun recordIn(answer: String): UnimarcRecord? {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
            val document = factory.newDocumentBuilder().parse(InputSource(StringReader(answer)))
            val record = document.getElementsByTagNameNS(MARCXCHANGE, "record").item(0) as Element?
            return record?.let { UnimarcRecord(controlsOf(it), fieldsOf(it)) }
        }

        fun controlsOf(record: Element): List<Pair<String, String>> =
            record.getElementsByTagNameNS(MARCXCHANGE, "controlfield").let { controls ->
                (0 until controls.length).map { index ->
                    (controls.item(index) as Element).let { it.getAttribute("tag") to it.textContent }
                }
            }

        fun fieldsOf(record: Element): List<UnimarcField> =
            record.getElementsByTagNameNS(MARCXCHANGE, "datafield").let { fields ->
                (0 until fields.length).map { index -> fieldOf(fields.item(index) as Element) }
            }

        fun fieldOf(field: Element): UnimarcField =
            field.getElementsByTagNameNS(MARCXCHANGE, "subfield").let { subfields ->
                UnimarcField(
                    tag = field.getAttribute("tag"),
                    subfields = (0 until subfields.length).map { index ->
                        (subfields.item(index) as Element).let { it.getAttribute("code") to it.textContent }
                    },
                )
            }
    }
}

private class UnimarcRecord(
    private val controls: List<Pair<String, String>>,
    private val fields: List<UnimarcField>,
) {
    fun control(tag: String): String? = controls.firstOrNull { it.first == tag }?.second

    fun value(tag: String, code: String): String? = fields.firstOrNull { it.tag == tag }?.value(code)

    fun fields(tags: Set<String>): List<UnimarcField> = fields.filter { it.tag in tags }
}

private class UnimarcField(val tag: String, private val subfields: List<Pair<String, String>>) {
    fun value(code: String): String? = subfields.firstOrNull { it.first == code }?.second
}
