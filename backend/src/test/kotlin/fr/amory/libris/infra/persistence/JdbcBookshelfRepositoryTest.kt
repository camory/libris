package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.Member
import fr.amory.libris.domain.MemberRole.OWNER
import fr.amory.libris.domain.Reader
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@JdbcSliceTest
@Import(JdbcBookshelfRepository::class, JdbcReaderRepository::class)
class JdbcBookshelfRepositoryTest @Autowired constructor(
    private val bookshelves: JdbcBookshelfRepository,
    private val readers: JdbcReaderRepository,
) {
    @Test
    fun `an inserted bookshelf is read back whole by the reader who owns it`() {
        // Given
        val lea = reader("lea", "Léa")
        val bookshelf = Bookshelf(name = "Bibliothèque de Léa", members = listOf(Member(lea.id, OWNER)))

        // When
        bookshelves.insert(bookshelf)

        // Then
        bookshelves.findByMember(lea.id) shouldBe listOf(bookshelf)
    }

    @Test
    fun `a reader reads the bookshelves they are a member of and no other`() {
        // Given
        val lea = reader("lea", "Léa")
        val marc = reader("marc", "Marc")
        val leas = Bookshelf(name = "Bibliothèque de Léa", members = listOf(Member(lea.id, OWNER)))
        val marcs = Bookshelf(name = "Bibliothèque de Marc", members = listOf(Member(marc.id, OWNER)))
        bookshelves.insert(leas)
        bookshelves.insert(marcs)

        // When
        val read = bookshelves.findByMember(lea.id)

        // Then
        read shouldBe listOf(leas)
    }

    @Test
    fun `a reader who is a member of no bookshelf reads none`() {
        // Given
        val marc = reader("marc", "Marc")
        bookshelves.insert(Bookshelf(name = "Bibliothèque de Marc", members = listOf(Member(marc.id, OWNER))))
        val lea = reader("lea", "Léa")

        // When, Then
        bookshelves.findByMember(lea.id) shouldBe emptyList()
    }

    private fun reader(username: String, displayName: String): Reader {
        val reader = Reader(username = username, email = "$username@amory.fr", displayName = displayName)
        readers.insert(reader)
        return reader
    }
}
