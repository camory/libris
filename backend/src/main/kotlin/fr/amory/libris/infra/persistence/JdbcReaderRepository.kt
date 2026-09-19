package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.domain.Member
import fr.amory.libris.domain.MemberRole
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val INSERT_READER =
    "insert into reader (id, username, email, display_name, default_bookshelf_id) " +
        "values (:id, :username, :email, :displayName, :defaultBookshelfId)"

private const val INSERT_MEMBERSHIP =
    "insert into reader_bookshelf (reader_id, bookshelf_id, role) values (:readerId, :bookshelfId, :role)"

private const val FIND_READER_BY_USERNAME =
    """
    select reader.id, reader.username, reader.email, reader.display_name, reader.default_bookshelf_id,
           reader_bookshelf.bookshelf_id, reader_bookshelf.role
    from reader
    join reader_bookshelf on reader_bookshelf.reader_id = reader.id
    where reader.username = :username
    order by reader_bookshelf.bookshelf_id
    """

private class ReaderRow(
    val id: UUID,
    val username: String,
    val email: String,
    val displayName: String,
    val defaultBookshelfId: UUID,
    val membership: Member,
)

@Repository
class JdbcReaderRepository(private val jdbcClient: JdbcClient) : ReaderRepository {
    override fun insert(reader: Reader) {
        try {
            jdbcClient
                .sql(INSERT_READER)
                .param("id", reader.id)
                .param("username", reader.username)
                .param("email", reader.email)
                .param("displayName", reader.displayName)
                .param("defaultBookshelfId", reader.defaultBookshelfId)
                .update()
        } catch (duplicate: DuplicateKeyException) {
            throw DuplicateUsernameException(reader.username, duplicate)
        }
        reader.memberships.forEach { membership ->
            jdbcClient
                .sql(INSERT_MEMBERSHIP)
                .param("readerId", reader.id)
                .param("bookshelfId", membership.bookshelfId)
                .param("role", membership.role.name)
                .update()
        }
    }

    override fun findByUsername(username: String): Reader? =
        jdbcClient
            .sql(FIND_READER_BY_USERNAME)
            .param("username", username)
            .query { rs, _ ->
                ReaderRow(
                    id = rs.getObject("id", UUID::class.java),
                    username = rs.getString("username"),
                    email = rs.getString("email"),
                    displayName = rs.getString("display_name"),
                    defaultBookshelfId = rs.getObject("default_bookshelf_id", UUID::class.java),
                    membership = Member(
                        bookshelfId = rs.getObject("bookshelf_id", UUID::class.java),
                        role = MemberRole.valueOf(rs.getString("role")),
                    ),
                )
            }
            .list()
            .takeIf { it.isNotEmpty() }
            ?.let { rows ->
                val first = rows.first()
                Reader(
                    id = first.id,
                    username = first.username,
                    email = first.email,
                    displayName = first.displayName,
                    memberships = rows.map { it.membership },
                    defaultBookshelfId = first.defaultBookshelfId,
                )
            }
}
