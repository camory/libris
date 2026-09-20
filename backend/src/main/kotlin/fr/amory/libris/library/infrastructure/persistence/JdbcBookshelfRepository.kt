package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val INSERT_BOOKSHELF = "insert into bookshelf (id, name) values (:id, :name)"

private const val INSERT_MEMBERSHIP =
    "insert into membership (bookshelf_id, reader_id, role) values (:bookshelfId, :readerId, :role)"

private const val FIND_BOOKSHELF_BY_ID =
    """
    select bookshelf.id, bookshelf.name, membership.reader_id, membership.role
    from bookshelf
    join membership on membership.bookshelf_id = bookshelf.id
    where bookshelf.id = :id
    order by membership.reader_id
    """

private class BookshelfRow(
    val id: BookshelfId,
    val name: String,
    val membership: Membership,
)

@Repository
class JdbcBookshelfRepository(private val jdbcClient: JdbcClient) : BookshelfRepository {
    override fun insert(bookshelf: Bookshelf) {
        jdbcClient
            .sql(INSERT_BOOKSHELF)
            .param("id", bookshelf.id.value)
            .param("name", bookshelf.name)
            .update()
        bookshelf.memberships.forEach { membership ->
            jdbcClient
                .sql(INSERT_MEMBERSHIP)
                .param("bookshelfId", bookshelf.id.value)
                .param("readerId", membership.readerId.value)
                .param("role", membership.role.name)
                .update()
        }
    }

    override fun findById(id: BookshelfId): Bookshelf? =
        jdbcClient
            .sql(FIND_BOOKSHELF_BY_ID)
            .param("id", id.value)
            .query { rs, _ ->
                BookshelfRow(
                    id = BookshelfId(rs.getObject("id", UUID::class.java)),
                    name = rs.getString("name"),
                    membership = Membership(
                        ReaderId(rs.getObject("reader_id", UUID::class.java)),
                        MembershipRole.valueOf(rs.getString("role")),
                    ),
                )
            }
            .list()
            .takeIf { it.isNotEmpty() }
            ?.let { rows -> Bookshelf(rows.first().id, rows.first().name, rows.map { it.membership }) }
}
