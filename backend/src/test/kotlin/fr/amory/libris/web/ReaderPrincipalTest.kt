package fr.amory.libris.web

import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

class ReaderPrincipalTest {
  @Test
  fun `the principal of a request is the reader its identity welcomes`() {
    // Given
    val juliette = readerNamed("juliette", "Juliette")
    val welcomeReader = mock(WelcomeReader::class.java)
    given(welcomeReader("juliette", "juliette@amory.fr", "Juliette")).willReturn(juliette)

    // When
    val principal = ReaderPrincipal(welcomeReader).of("juliette", "juliette@amory.fr", "Juliette")

    // Then
    principal shouldBe juliette
  }
}
