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
      "Français & Français : l'eau \"vive\" <3>"
  }
}
