package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.MemberProfile
import fr.amory.libris.domain.MemberProfileRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JdbcMemberProfileRepository(private val jdbc: JdbcClient) : MemberProfileRepository {
    override fun insert(profile: MemberProfile) {
        jdbc.sql("insert into member (id, username, display_name) values (:id, :username, :displayName)")
            .param("id", profile.id)
            .param("username", profile.username)
            .param("displayName", profile.displayName)
            .update()
    }

    override fun findByUsername(username: String): MemberProfile? =
        jdbc.sql("select id, username, display_name from member where username = :username")
            .param("username", username)
            .query { rs, _ ->
                MemberProfile(
                    rs.getString("username"),
                    rs.getString("display_name"),
                    rs.getObject("id", UUID::class.java),
                )
            }
            .optional()
            .orElse(null)
}
