package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val INSERT_READER =
    "insert into reader (id, username, email, display_name, default_bookshelf_id) " +
        "values (:id, :username, :email, :displayName, :defaultBookshelfId)"

private const val UPDATE_READER =
    "update reader set username = :username, email = :email, display_name = :displayName, " +
        "default_bookshelf_id = :defaultBookshelfId where id = :id"

private const val FIND_READER_BY_USERNAME =
    "select id, username, email, display_name, default_bookshelf_id from reader where username = :username"

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
    }

    override fun update(reader: Reader) {
        jdbcClient
            .sql(UPDATE_READER)
            .param("id", reader.id)
            .param("username", reader.username)
            .param("email", reader.email)
            .param("displayName", reader.displayName)
            .param("defaultBookshelfId", reader.defaultBookshelfId)
            .update()
    }

    override fun findByUsername(username: String): Reader? =
        jdbcClient
            .sql(FIND_READER_BY_USERNAME)
            .param("username", username)
            .query { rs, _ ->
                Reader(
                    id = rs.getObject("id", UUID::class.java),
                    username = rs.getString("username"),
                    email = rs.getString("email"),
                    displayName = rs.getString("display_name"),
                    defaultBookshelfId = rs.getObject("default_bookshelf_id", UUID::class.java),
                )
            }
            .optional()
            .orElse(null)
}
