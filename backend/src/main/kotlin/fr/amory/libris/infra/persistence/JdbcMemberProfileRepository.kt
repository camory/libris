package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.MemberProfile
import fr.amory.libris.domain.MemberProfileRepository
import org.springframework.data.auditing.IsNewAwareAuditingHandler
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class JdbcMemberProfileRepository(
    private val aggregates: JdbcAggregateTemplate,
    private val auditing: IsNewAwareAuditingHandler,
) : MemberProfileRepository {
    override fun insert(profile: MemberProfile): MemberProfile = aggregates.insert(auditing.markCreated(profile))

    override fun findByUsername(username: String): MemberProfile? =
        aggregates
            .findOne(Query.query(Criteria.where("username").`is`(username)), MemberProfile::class.java)
            .orElse(null)
}
