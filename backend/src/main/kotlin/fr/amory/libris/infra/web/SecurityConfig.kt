package fr.amory.libris.infra.web

import fr.amory.libris.domain.Member
import fr.amory.libris.domain.Role
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.util.matcher.RequestMatcher

const val ADMIN_GROUP = "libris-admin"

class MemberPrincipal(val member: Member) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${member.role}"))

    override fun getPassword(): String? = null

    override fun getUsername(): String = member.username
}

class RemoteHeaderAuthenticationFilter : AbstractPreAuthenticatedProcessingFilter() {
    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any? {
        val username = request.getHeader("Remote-User")
        val email = request.getHeader("Remote-Email")?.takeUnless { it.isBlank() }
        if (username == null || email == null) return null
        val groups = request.getHeader("Remote-Groups").orEmpty().split(",").map { it.trim() }
        return Member(
            username = username,
            displayName = request.getHeader("Remote-Name") ?: username,
            email = email,
            role = if (ADMIN_GROUP in groups) Role.ADMIN else Role.MEMBER,
        )
    }

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest): Any = "N/A"
}

@Configuration
class SecurityConfig {
    @Bean
    fun authenticationManager(): AuthenticationManager {
        val provider = PreAuthenticatedAuthenticationProvider()
        provider.setPreAuthenticatedUserDetailsService { token ->
            MemberPrincipal(token.principal as Member)
        }
        return ProviderManager(provider)
    }

    @Bean
    fun filterChain(http: HttpSecurity, authenticationManager: AuthenticationManager): SecurityFilterChain {
        val filter = RemoteHeaderAuthenticationFilter()
        filter.setAuthenticationManager(authenticationManager)
        val unsafeWrite = RequestMatcher {
            it.method != HttpMethod.GET.name() && it.getHeader("X-Requested-With") == null
        }
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(filter, AbstractPreAuthenticatedProcessingFilter::class.java)
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers(unsafeWrite).denyAll()
                it.anyRequest().authenticated()
            }
            .build()
    }
}
