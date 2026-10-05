package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.TRANSLATOR
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.Kind.MANGA
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BnfRecordTest {
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
  fun `a control field without an ark names none`() {
    arkOf("http://catalogue.bnf.fr/ark:/12148/cb43636708p") shouldBe "ark:/12148/cb43636708p"
    arkOf("FRBNF436367080000000") shouldBe null
    arkOf(null) shouldBe null
  }
}
