package fr.amory.libris.bibliography.infrastructure.lookup

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BnfSummaryTest {
  @Test
  fun `markup is dropped`() {
    summaryOf("<p><b>Le Mont</b> <i>Saint-Michel</i></p>") shouldBe "Le Mont Saint-Michel"
  }

  @Test
  fun `an entity of the markup is its character`() {
    summaryOf("Fran&#231;ais &amp; Fran&#xE7;ais&nbsp;: l&apos;eau &quot;vive&quot; &lt;3&gt;") shouldBe
      "Français & Français\u00A0: l'eau \"vive\" <3>"
  }

  @Test
  fun `a numeric reference that names no character stays as written`() {
    summaryOf("&#1114112; &#99999999999; &#x123456789;") shouldBe "&#1114112; &#99999999999; &#x123456789;"
  }

  @Test
  fun `a decoded lower-than sign is text, not markup`() {
    summaryOf("Moins de 10 &lt;b&gt; ici") shouldBe "Moins de 10 <b> ici"
  }

  @Test
  fun `a paragraph end followed by a new paragraph is a line feed`() {
    summaryOf("<p>Un</p><p>Deux</p>") shouldBe "Un\nDeux"
  }

  @Test
  fun `a paragraph end followed by spaces and a new paragraph is a line feed`() {
    summaryOf("<p>Un</p> <p>Deux</p>") shouldBe "Un\nDeux"
  }

  @Test
  fun `a markup line break is a line feed`() {
    summaryOf("Un<br/>Deux<br>Trois<br />Quatre") shouldBe "Un\nDeux\nTrois\nQuatre"
  }

  @Test
  fun `a run of line breaks is one line feed`() {
    summaryOf("<p>Un</p>\n<p>Deux<br/><br/>Trois\n\nQuatre</p>") shouldBe "Un\nDeux\nTrois\nQuatre"
  }

  @Test
  fun `a run of spaces is one space`() {
    summaryOf("épi...\u00A0 Issu   d'une") shouldBe "épi... Issu d'une"
  }

  @Test
  fun `a lone non-breaking space is kept`() {
    summaryOf("leurre\u00A0? Une") shouldBe "leurre\u00A0? Une"
  }

  @Test
  fun `the summary is trimmed`() {
    summaryOf("\n <p> Un </p>\n ") shouldBe "Un"
  }

  @Test
  fun `a space is put after a mark glued to a capital`() {
    summaryOf("pain.DÉCOUVREZ Oui!Non?Peut-être…Enfin") shouldBe "pain. DÉCOUVREZ Oui! Non? Peut-être… Enfin"
  }

  @Test
  fun `a space is put after a mark glued to a bullet`() {
    summaryOf("ÉTAPE.• LES épi...• LES") shouldBe "ÉTAPE. • LES épi... • LES"
  }

  @Test
  fun `a mark followed by a lower-case letter or a digit stays glued`() {
    summaryOf("version 2.0.1 sur bnf.fr") shouldBe "version 2.0.1 sur bnf.fr"
  }

  @Test
  fun `a summary that cleans to nothing is none`() {
    summaryOf("<p> </p><br/>") shouldBe null
  }
}
