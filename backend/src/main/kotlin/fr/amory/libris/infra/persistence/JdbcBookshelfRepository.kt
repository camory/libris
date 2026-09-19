package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.BookshelfRepository
import fr.amory.libris.domain.Member
import fr.amory.libris.domain.MemberRole
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val INSERT_BOOKSHELF = "insert into bookshelf (id, name) values (:id, :name)"

private const val INSERT_MEMBER =
    "insert into bookshelf_member (bookshelf_id, reader_id, role) values (:bookshelfId, :readerId, :role)"

private const val BOOKSHELVES_OF_MEMBER =
    """
    select bookshelf.id, bookshelf.name, bookshelf_member.reader_id, bookshelf_member.role
    from bookshelf
    join bookshelf_member on bookshelf_member.bookshelf_id = bookshelf.id
    where bookshelf.id in (select bookshelf_id from bookshelf_member where reader_id = :readerId)
    order by bookshelf.name, bookshelf_member.reader_id
    """

private class MemberRow(val bookshelfId: UUID, val bookshelfName: String, val member: Member)

@Repository
class JdbcBookshelfRepository(private val jdbcClient: JdbcClient) : BookshelfRepository {
    override fun insert(bookshelf: Bookshelf) {
        jdbcClient
            .sql(INSERT_BOOKSHELF)
            .param("id", bookshelf.id)
            .param("name", bookshelf.name)
            .update()
        bookshelf.members.forEach { member ->
            jdbcClient
                .sql(INSERT_MEMBER)
                .param("bookshelfId", bookshelf.id)
                .param("readerId", member.readerId)
                .param("role", member.role.name)
                .update()
        }
    }

    override fun findByMember(readerId: UUID): List<Bookshelf> =
        jdbcClient
            .sql(BOOKSHELVES_OF_MEMBER)
            .param("readerId", readerId)
            .query { rs, _ ->
                MemberRow(
                    bookshelfId = rs.getObject("id", UUID::class.java),
                    bookshelfName = rs.getString("name"),
                    member = Member(
                        readerId = rs.getObject("reader_id", UUID::class.java),
                        role = MemberRole.valueOf(rs.getString("role")),
                    ),
                )
            }
            .list()
            .groupBy { it.bookshelfId }
            .map { (id, rows) -> Bookshelf(id, rows.first().bookshelfName, rows.map { it.member }) }
}
