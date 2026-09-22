package fr.amory.libris.bibliography.infrastructure.lookup

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.TRANSLATOR
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Failed
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.NothingKnown
import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

class BnfEditionLookupTest {
    private val source = BnfEditionLookup(server.baseUrl() + SRU, TIMEOUT)

    @AfterEach
    fun forgetTheStubs() {
        server.resetAll()
    }

    @Test
    fun `a known ISBN is what the BnF knows about it`() {
        // Given
        bnf.knows(ONE_PIECE)

        // When
        val answer = source.lookUp(isbnOf(ONE_PIECE))

        // Then
        answer shouldBe Known(ONE_PIECE_EDITION)
    }

    @Test
    fun `a record whose publisher sits in field 214 is what the BnF knows about it`() {
        // Given
        bnf.knows(NERONIA)

        // When
        val answer = source.lookUp(isbnOf(NERONIA))

        // Then
        answer shouldBe Known(
            EditionPreview(
                isbn = isbnOf(NERONIA),
                kind = BD,
                title = "Les Neronia",
                subtitle = null,
                contributions = Contributions.of(
                    listOf(
                        Contribution("Jean Dufaux", WRITER),
                        Contribution("Jérémy", ARTIST),
                    ),
                ),
                series = SeriesEntry("Murena", 13),
                collection = null,
                publisher = "Dargaud Benelux",
                publicationYear = 2025,
                language = "fr",
                pageCount = 52,
                summary = null,
                coverUrl = NERONIA_COVER,
            ),
        )
    }

    @Test
    fun `a record with a printer's 214 beside the publisher's takes the publisher's`() {
        // Given
        bnf.knows(LEMURIA)

        // When
        val answer = source.lookUp(isbnOf(LEMURIA))

        // Then
        answer shouldBe Known(
            EditionPreview(
                isbn = isbnOf(LEMURIA),
                kind = BD,
                title = "Lemuria",
                subtitle = null,
                contributions = Contributions.of(
                    listOf(
                        Contribution("Jean Dufaux", WRITER),
                        Contribution("Philippe Delaby", WRITER),
                        Contribution("Théo", WRITER),
                    ),
                ),
                series = SeriesEntry("Murena", 11),
                collection = null,
                publisher = "Dargaud Benelux",
                publicationYear = 2020,
                language = "fr",
                pageCount = 50,
                summary = null,
                coverUrl = LEMURIA_COVER,
            ),
        )
    }

    @Test
    fun `a provisional record is what the BnF knows about it`() {
        // Given
        bnf.knows(APOTHICAIRE)

        // When
        val answer = source.lookUp(isbnOf(APOTHICAIRE))

        // Then
        answer shouldBe Known(
            EditionPreview(
                isbn = isbnOf(APOTHICAIRE),
                kind = BOOK,
                title = "Les Carnets de l'apothicaire",
                subtitle = null,
                contributions = Contributions.of(listOf(Contribution("Natsu Hyūga", WRITER))),
                series = SeriesEntry("Les Carnets de l'apothicaire", 7),
                collection = null,
                publisher = "Lumen",
                publicationYear = 2026,
                language = "fr",
                pageCount = 348,
                summary = null,
                coverUrl = APOTHICAIRE_COVER,
            ),
        )
    }

    @Test
    fun `a contributor or a series without a name is left out`() {
        // Given
        bnf.answers(BLANK_NAMES, CONTRIBUTOR_AND_SERIES_WITHOUT_A_NAME)

        // When
        val answer = source.lookUp(isbnOf(BLANK_NAMES))

        // Then
        val preview = (answer as Known).preview
        preview.contributions.all shouldBe listOf(Contribution("Eiichirō Oda", WRITER))
        preview.series shouldBe null
    }

    @Test
    fun `a person listed under two codes the house reads as one role is one contribution`() {
        // Given
        bnf.answers(BLANK_NAMES, ONE_PERSON_TWICE)

        // When
        val answer = source.lookUp(isbnOf(BLANK_NAMES))

        // Then
        (answer as Known).preview.contributions.all shouldBe listOf(Contribution("Eiichirō Oda", WRITER))
    }

    @Test
    fun `the publisher is the 214 the second indicator marks, wherever it sits`() {
        // Given
        bnf.answers(PRINTER_FIRST, PRINTER_BEFORE_PUBLISHER)

        // When
        val answer = source.lookUp(isbnOf(PRINTER_FIRST))

        // Then
        answer shouldBe Known(
            EditionPreview(
                isbn = isbnOf(PRINTER_FIRST),
                kind = BOOK,
                title = "Un livre",
                subtitle = null,
                contributions = Contributions.of(emptyList()),
                series = null,
                collection = null,
                publisher = "Dargaud Benelux",
                publicationYear = null,
                language = null,
                pageCount = null,
                summary = null,
                coverUrl = null,
            ),
        )
    }

    @Test
    fun `a record without pages and year gives neither`() {
        // Given
        bnf.partiallyKnows(ONE_PIECE)

        // When
        val answer = source.lookUp(isbnOf(ONE_PIECE))

        // Then
        answer shouldBe Known(ONE_PIECE_EDITION.copy(publicationYear = null, pageCount = null))
    }

    @Test
    fun `an ISBN the BnF does not know is nothing known`() {
        // Given
        bnf.doesNotKnow(UNKNOWN)

        // When
        val answer = source.lookUp(isbnOf(UNKNOWN))

        // Then
        answer shouldBe NothingKnown
    }

    @Test
    fun `a BnF that fails is a failure`() {
        // Given
        bnf.fails()

        // When
        val answer = source.lookUp(isbnOf(ONE_PIECE))

        // Then
        answer shouldBe Failed
    }

    @Test
    fun `an answer that cannot be read is a failure`() {
        // Given
        bnf.answersUnreadably(ONE_PIECE)

        // When
        val answer = source.lookUp(isbnOf(ONE_PIECE))

        // Then
        answer shouldBe Failed
    }

    @Test
    fun `a BnF that answers past the timeout is a failure`() {
        // Given
        bnf.answersTooLate(ONE_PIECE)

        // When
        val answer = source.lookUp(isbnOf(ONE_PIECE))

        // Then
        answer shouldBe Failed
    }

    @Test
    fun `the kind is what the form of contents and the language translated from name`() {
        kindOf("||||t   00|a|", "jpn") shouldBe MANGA
        kindOf("||||t   00|a|", "kor") shouldBe MANGA
        kindOf("||||t   00|a|", "chi") shouldBe MANGA
        kindOf("||||t   00|a|", "eng") shouldBe BD
        kindOf("||||t   00|a|", null) shouldBe BD
        kindOf("||||z   00|||", null) shouldBe BOOK
        kindOf(null, null) shouldBe BOOK
    }

    @Test
    fun `an author's role is what their function code names`() {
        contributionRoleOf("070") shouldBe WRITER
        contributionRoleOf("440") shouldBe ARTIST
        contributionRoleOf("730") shouldBe TRANSLATOR
        contributionRoleOf(null) shouldBe WRITER
        contributionRoleOf("999") shouldBe WRITER
    }

    @Test
    fun `the publication year is what the date of publication names`() {
        publicationYearOf("20251023d2025    m  y0frey50      ba", "DL 2025") shouldBe 2025
        publicationYearOf("20260630u2026    a  y0frey50      ba", null) shouldBe 2026
        publicationYearOf(null, "impr. 2013") shouldBe 2013
        publicationYearOf("2025", "DL 2020") shouldBe 2020
        publicationYearOf(null, null) shouldBe null
    }

    @Test
    fun `a tome is read from the title statement only when it names one`() {
        tomeOf("tome 7") shouldBe 7
        tomeOf("chapitre treizième") shouldBe null
        tomeOf(null) shouldBe null
    }

    @Test
    fun `a page count is read from the extent, plates not being pages`() {
        pageCountOf("1 vol. (203 p.)") shouldBe 203
        pageCountOf("1 volume 348 p") shouldBe 348
        pageCountOf("1 vol. (32 pl.)") shouldBe null
        pageCountOf(null) shouldBe null
    }

    @Test
    fun `a control field without an ark names no cover`() {
        coverUrlOf("http://catalogue.bnf.fr/ark:/12148/cb43636708p") shouldBe ONE_PIECE_COVER
        coverUrlOf("FRBNF436367080000000") shouldBe null
        coverUrlOf(null) shouldBe null
    }

    @Test
    fun `looking up an ISBN sends one search on the SRU endpoint`() {
        // Given
        bnf.knows(ONE_PIECE)

        // When
        source.lookUp(isbnOf(ONE_PIECE))

        // Then
        val request = server.allServeEvents.map { it.request }.single()
        request.method shouldBe GET
        request.url.substringBefore("?") shouldBe SRU
        request.queryParameter("version").values() shouldBe listOf("1.2")
        request.queryParameter("operation").values() shouldBe listOf("searchRetrieve")
        request.queryParameter("recordSchema").values() shouldBe listOf("unimarcxchange")
        request.queryParameter("maximumRecords").values() shouldBe listOf("1")
        request.queryParameter("query").values() shouldBe
            listOf("""bib.isbn all "$ONE_PIECE" or bib.isbn all "$ONE_PIECE_TEN"""")
    }

    @Test
    fun `an ISBN that does not start with 978 is searched on its thirteen digits alone`() {
        // Given
        bnf.fails()

        // When
        source.lookUp(isbnOf(WITHOUT_A_TEN))

        // Then
        val request = server.allServeEvents.map { it.request }.single()
        request.queryParameter("query").values() shouldBe listOf("""bib.isbn all "$WITHOUT_A_TEN"""")
    }

    private companion object {
        const val ONE_PIECE = "9782723488525"
        const val ONE_PIECE_TEN = "2723488527"
        const val NERONIA = "9782505125990"
        const val LEMURIA = "9782505083399"
        const val APOTHICAIRE = "9782371025219"
        const val PRINTER_FIRST = "9782999999992"
        const val UNKNOWN = "9782000000013"
        const val WITHOUT_A_TEN = "9791000000008"
        const val SRU = "/api/SRU"
        const val ONE_PIECE_COVER =
            "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb43636708p&couverture=1"
        const val NERONIA_COVER =
            "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb486302521&couverture=1"
        const val LEMURIA_COVER =
            "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb46810645h&couverture=1"
        const val APOTHICAIRE_COVER =
            "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb48801192z&couverture=1"
        const val BLANK_NAMES = "9782723489898"
        val CONTRIBUTOR_AND_SERIES_WITHOUT_A_NAME = """
            <srw:searchRetrieveResponse xmlns:srw="http://www.loc.gov/zing/srw/">
            <mxc:record xmlns:mxc="info:lc/xmlns/marcxchange-v2">
            <mxc:datafield tag="200" ind1="1" ind2=" ">
            <mxc:subfield code="a">Aux prises avec Baggy et ses hommes</mxc:subfield>
            </mxc:datafield>
            <mxc:datafield tag="461" ind1=" " ind2=" ">
            <mxc:subfield code="t"> </mxc:subfield>
            <mxc:subfield code="v">2</mxc:subfield>
            </mxc:datafield>
            <mxc:datafield tag="700" ind1=" " ind2="|">
            <mxc:subfield code="a"> </mxc:subfield>
            <mxc:subfield code="4">070</mxc:subfield>
            </mxc:datafield>
            <mxc:datafield tag="700" ind1=" " ind2="|">
            <mxc:subfield code="a">Oda</mxc:subfield>
            <mxc:subfield code="b">Eiichirō</mxc:subfield>
            <mxc:subfield code="4">070</mxc:subfield>
            </mxc:datafield>
            </mxc:record>
            </srw:searchRetrieveResponse>
        """.trimIndent()
        val ONE_PERSON_TWICE = """
            <srw:searchRetrieveResponse xmlns:srw="http://www.loc.gov/zing/srw/">
            <mxc:record xmlns:mxc="info:lc/xmlns/marcxchange-v2">
            <mxc:datafield tag="200" ind1="1" ind2=" ">
            <mxc:subfield code="a">Aux prises avec Baggy et ses hommes</mxc:subfield>
            </mxc:datafield>
            <mxc:datafield tag="700" ind1=" " ind2="|">
            <mxc:subfield code="a">Oda</mxc:subfield>
            <mxc:subfield code="b">Eiichirō</mxc:subfield>
            <mxc:subfield code="4">070</mxc:subfield>
            </mxc:datafield>
            <mxc:datafield tag="702" ind1=" " ind2="|">
            <mxc:subfield code="a">Oda</mxc:subfield>
            <mxc:subfield code="b">Eiichirō</mxc:subfield>
            <mxc:subfield code="4">080</mxc:subfield>
            </mxc:datafield>
            </mxc:record>
            </srw:searchRetrieveResponse>
        """.trimIndent()
        val PRINTER_BEFORE_PUBLISHER = """
            <srw:searchRetrieveResponse xmlns:srw="http://www.loc.gov/zing/srw/">
            <mxc:record xmlns:mxc="info:lc/xmlns/marcxchange-v2">
            <mxc:datafield tag="200" ind1="1" ind2=" ">
            <mxc:subfield code="a">Un livre</mxc:subfield>
            </mxc:datafield>
            <mxc:datafield tag="214" ind1=" " ind2="3">
            <mxc:subfield code="c">Impr. PPO graphic</mxc:subfield>
            </mxc:datafield>
            <mxc:datafield tag="214" ind1=" " ind2="0">
            <mxc:subfield code="c">Dargaud Benelux</mxc:subfield>
            </mxc:datafield>
            </mxc:record>
            </srw:searchRetrieveResponse>
        """.trimIndent()
        val ONE_PIECE_EDITION = EditionPreview(
            isbn = isbnOf(ONE_PIECE),
            kind = MANGA,
            title = "Romance dawn",
            subtitle = "à l'aube d'une grande aventure",
            contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
            series = SeriesEntry("One piece", 1),
            collection = "Shonen manga",
            publisher = "Glénat",
            publicationYear = 2013,
            language = "fr",
            pageCount = 203,
            summary = null,
            coverUrl = ONE_PIECE_COVER,
        )
        val TIMEOUT: Duration = ofMillis(200)
        val WARM_UP_TIMEOUT: Duration = ofSeconds(20)
        val server = WireMockServer(options().dynamicPort())
        val bnf = BnfStubs(server)

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            server.start()
            bnf.knows(ONE_PIECE)
            BnfEditionLookup(server.baseUrl() + SRU, WARM_UP_TIMEOUT).lookUp(isbnOf(ONE_PIECE))
            server.resetAll()
        }

        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            server.stop()
        }
    }
}
