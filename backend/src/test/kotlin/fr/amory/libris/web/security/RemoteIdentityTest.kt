package fr.amory.libris.web.security

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.text.Charsets.ISO_8859_1
import kotlin.text.Charsets.UTF_8

class RemoteIdentityTest {
  @Test
  fun `a request names its reader`() {
    // Given
    val request = requestOf("Remote-User" to "juliette", "Remote-Email" to "juliette@amory.fr")

    // When
    val identity = RemoteIdentity.of(request).shouldNotBeNull()

    // Then
    identity.username shouldBe "juliette"
    identity.email shouldBe "juliette@amory.fr"
  }

  @Test
  fun `a blank username names no reader`() {
    RemoteIdentity.of(requestOf("Remote-User" to " ", "Remote-Email" to "juliette@amory.fr")) shouldBe null
  }

  @Test
  fun `a username sent in UTF-8 is read whole`() {
    // Given
    val asTomcatReadsIt = String("léa".toByteArray(UTF_8), ISO_8859_1)
    val request = requestOf("Remote-User" to asTomcatReadsIt, "Remote-Email" to "lea@amory.fr")

    // When
    val identity = RemoteIdentity.of(request).shouldNotBeNull()

    // Then
    identity.username shouldBe "léa"
  }

  @Test
  fun `an email sent in UTF-8 is read whole`() {
    // Given
    val asTomcatReadsIt = String("léa@amory.fr".toByteArray(UTF_8), ISO_8859_1)
    val request = requestOf("Remote-User" to "juliette", "Remote-Email" to asTomcatReadsIt)

    // When
    val identity = RemoteIdentity.of(request).shouldNotBeNull()

    // Then
    identity.email shouldBe "léa@amory.fr"
  }

  @Test
  fun `the display name is the name header`() {
    // Given
    val request = julietteSending("Remote-Name" to "Juliette")

    // When
    val identity = RemoteIdentity.of(request).shouldNotBeNull()

    // Then
    identity.displayName shouldBe "Juliette"
  }

  @Test
  fun `a name sent in UTF-8 is read whole`() {
    // Given
    val asTomcatReadsIt = String("Léa".toByteArray(UTF_8), ISO_8859_1)
    val request = julietteSending("Remote-Name" to asTomcatReadsIt)

    // When
    val identity = RemoteIdentity.of(request).shouldNotBeNull()

    // Then
    identity.displayName shouldBe "Léa"
  }

  @Test
  fun `without a name, the reader is their username`() {
    RemoteIdentity.of(julietteSending()).shouldNotBeNull().displayName shouldBe "juliette"
  }

  @Test
  fun `a blank name is no name`() {
    RemoteIdentity.of(julietteSending("Remote-Name" to " ")).shouldNotBeNull().displayName shouldBe "juliette"
  }

  @Test
  fun `without groups, a reader holds the reader authority alone`() {
    authoritiesOf() shouldBe listOf("ROLE_READER")
  }

  @Test
  fun `a member of libris-admin is also an admin`() {
    authoritiesOf("Remote-Groups" to "family,libris-admin") shouldBe listOf("ROLE_READER", "ROLE_ADMIN")
  }

  @Test
  fun `a group after a comma and a space is read`() {
    authoritiesOf("Remote-Groups" to "family, libris-admin") shouldBe listOf("ROLE_READER", "ROLE_ADMIN")
  }

  @Test
  fun `a group set off by a non-breaking space is read`() {
    // Given
    val asTomcatReadsIt = String("family,\u00A0libris-admin".toByteArray(UTF_8), ISO_8859_1)

    // When / Then
    authoritiesOf("Remote-Groups" to asTomcatReadsIt) shouldBe listOf("ROLE_READER", "ROLE_ADMIN")
  }

  @Test
  fun `another group is no admin`() {
    authoritiesOf("Remote-Groups" to "family") shouldBe listOf("ROLE_READER")
  }

  private fun authoritiesOf(vararg headers: Pair<String, String>): List<String?> =
    RemoteIdentity.of(julietteSending(*headers)).shouldNotBeNull().authorities.map { it.authority }

  private fun julietteSending(vararg headers: Pair<String, String>): MockHttpServletRequest =
    requestOf("Remote-User" to "juliette", "Remote-Email" to "juliette@amory.fr", *headers)

  private fun requestOf(vararg headers: Pair<String, String>): MockHttpServletRequest =
    MockHttpServletRequest().apply { headers.forEach { (name, value) -> addHeader(name, value) } }
}
