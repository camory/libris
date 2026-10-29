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
  fun `a decoded lower-than sign is text, not markup`() {
    summaryOf("Moins de 10 &lt;b&gt; ici") shouldBe "Moins de 10 <b> ici"
  }

  @Test
  fun `a paragraph end followed by a new paragraph is a line feed`() {
    summaryOf("<p>Un</p><p>Deux</p>") shouldBe "Un\nDeux"
  }

  @Test
  fun `a markup line break is a line feed`() {
    summaryOf("Un<br/>Deux<br>Trois<br />Quatre") shouldBe "Un\nDeux\nTrois\nQuatre"
  }
}
