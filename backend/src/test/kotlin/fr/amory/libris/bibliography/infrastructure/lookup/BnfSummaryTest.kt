package fr.amory.libris.bibliography.infrastructure.lookup

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BnfSummaryTest {
  @Test
  fun `markup is dropped`() {
    summaryOf("<p><b>Le Mont</b> <i>Saint-Michel</i></p>") shouldBe "Le Mont Saint-Michel"
  }
}
