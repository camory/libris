package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.fixture.AuthorNamesAnswering
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

class ListAuthorNamesTest {
  @Test
  fun `the name of every author of the house is answered`() {
    // Given
    val listAuthorNames = ListAuthorNames(AuthorNamesAnswering(listOf("Hergé", "René Goscinny")))

    // When
    val names = listAuthorNames()

    // Then
    names shouldContainExactlyInAnyOrder listOf("Hergé", "René Goscinny")
  }
}
