package fr.amory.libris.infra.lookup

import fr.amory.libris.domain.AuthorRole
import fr.amory.libris.domain.AuthorRole.ARTIST
import fr.amory.libris.domain.AuthorRole.TRANSLATOR
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn
import fr.amory.libris.domain.lookup.IsbnSource
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
private val PAGES = Regex("(\\d+)\\s*p\\b")
private val TOME = Regex("tome (\\d+)")
private const val YEAR_AT = 9
private const val YEAR_LENGTH = 4
private const val PUBLISHER_INDICATOR = "0"
private const val ARK = "ark:/"
private const val COVER_BEFORE = "https://catalogue.bnf.fr/couverture?&appName=NE&idArk="
private const val COVER_AFTER = "&couverture=1"

internal fun authorRoleOf(functionCode: String?): AuthorRole = ROLES[functionCode] ?: WRITER

internal fun publicationYearOf(dateOfPublication: String?, publication: String?): Int? =
    dateOfPublication?.drop(YEAR_AT)?.take(YEAR_LENGTH)?.takeIf { YEAR.matches(it) }?.toInt()
        ?: YEAR.find(publication.orEmpty())?.value?.toIntOrNull()

internal fun tomeOf(partNumber: String?): Int? =
    TOME.find(partNumber.orEmpty())?.groupValues?.get(1)?.toIntOrNull()

internal fun pageCountOf(extent: String?): Int? =
    PAGES.find(extent.orEmpty())?.groupValues?.get(1)?.toIntOrNull()

internal fun coverUrlOf(controlField: String?): String? =
    controlField?.indexOf(ARK)?.takeIf { it >= 0 }
        ?.let { COVER_BEFORE + controlField.substring(it) + COVER_AFTER }

class BnfSource(baseUrl: String, timeout: Duration) : IsbnSource {
    private val http = sourceRestClient(baseUrl, timeout)

    override fun lookUp(isbn: Isbn): SourceAnswer =
        try {
            answerFor(isbn)
        } catch (ignored: RestClientException) {
            Failed
        } catch (ignored: SAXException) {
            Failed
        }

    private fun answerFor(isbn: Isbn): SourceAnswer =
        recordIn(search(isbn))?.let { answerFrom(isbn, it) } ?: NothingKnown

    private fun answerFrom(isbn: Isbn, record: UnimarcRecord): SourceAnswer =
        record.value("200", "a")?.let { Known(editionOf(isbn, it, record)) } ?: Failed

    private fun editionOf(isbn: Isbn, title: String, record: UnimarcRecord): SourceEdition {
        val publication = publicationOf(record)
        return SourceEdition(
            isbn = isbn,
            title = title,
            subtitle = record.value("200", "e"),
            authors = authorsOf(record),
            series = seriesOf(record),
            collection = record.value("410", "t"),
            publisher = publication?.value("c"),
            publicationYear = publicationYearOf(record.value("100", "a"), publication?.value("d")),
            language = LANGUAGES[record.value("101", "a")],
            pageCount = pageCountOf(record.value("215", "a")),
            summary = null,
            coverUrl = coverUrlOf(record.control("003")),
        )
    }

    private fun publicationOf(record: UnimarcRecord): UnimarcField? =
        record.field("214", PUBLISHER_INDICATOR) ?: record.field("210")

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
            ?: titleStatementSeriesOf(record)

    private fun titleStatementSeriesOf(record: UnimarcRecord): SourceSeries? =
        tomeOf(record.value("200", "h"))?.let { tome ->
            record.value("200", "a")?.let { SourceSeries(it, tome) }
        }

    private fun search(isbn: Isbn): String = http.get()
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
        fun queryFor(isbn: Isbn): String =
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
                    indicator2 = field.getAttribute("ind2"),
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

    fun value(tag: String, code: String): String? = field(tag)?.value(code)

    fun field(tag: String): UnimarcField? = fields.firstOrNull { it.tag == tag }

    fun field(tag: String, indicator2: String): UnimarcField? =
        fields.firstOrNull { it.tag == tag && it.indicator2 == indicator2 }

    fun fields(tags: Set<String>): List<UnimarcField> = fields.filter { it.tag in tags }
}

private class UnimarcField(
    val tag: String,
    val indicator2: String,
    private val subfields: List<Pair<String, String>>,
) {
    fun value(code: String): String? = subfields.firstOrNull { it.first == code }?.second
}
