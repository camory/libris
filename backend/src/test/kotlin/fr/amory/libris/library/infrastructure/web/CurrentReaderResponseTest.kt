package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority

class CurrentReaderResponseTest {
  @Test
  fun `the reader authority alone is a plain reader`() {
    // Given
    val juliette = readerNamed("juliette", "Juliette")

    // When
    val response = CurrentReaderResponse.from(
      juliette,
      bookshelfOwnedBy(juliette),
      listOf(SimpleGrantedAuthority("ROLE_READER")),
    )

    // Then
    response.role shouldBe Role.READER
  }
}
