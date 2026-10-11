package fr.amory.libris.library.application

import fr.amory.libris.library.fixture.ReadersInMemory
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

class ListReadersTest {
  @Test
  fun `every reader Libris knows is answered`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val marc = readerNamed("marc", "Marc")
    val readers = ReadersInMemory().apply {
      insert(lea)
      insert(marc)
    }
    val listReaders = ListReaders(readers)

    // When
    val answer = listReaders()

    // Then
    answer shouldContainExactlyInAnyOrder listOf(lea, marc)
  }
}
