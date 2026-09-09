package fr.amory.libris.infra.web

import fr.amory.libris.application.ReaderVisit
import fr.amory.libris.domain.Reader
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

private const val ADMIN_GROUP = "libris-admin"

const val READER_AUTHORITY = "ROLE_READER"
const val ADMIN_AUTHORITY = "ROLE_ADMIN"

class ReaderPrincipal(
    val reader: Reader,
    private val authorities: Collection<GrantedAuthority>,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String? = null

    override fun getUsername(): String = reader.username
}

class RemoteHeaderAuthenticationFilter(private val readerVisit: ReaderVisit) :
    AbstractPreAuthenticatedProcessingFilter() {
    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any? {
        val username = request.getHeader("Remote-User")
        val email = request.getHeader("Remote-Email")?.takeUnless { it.isBlank() }
        if (username == null || email == null) return null
        val groups = request.getHeader("Remote-Groups").orEmpty().split(",").map { it.trim() }
        val authorities = buildList {
            add(SimpleGrantedAuthority(READER_AUTHORITY))
            if (ADMIN_GROUP in groups) add(SimpleGrantedAuthority(ADMIN_AUTHORITY))
        }
        val displayName = request.getHeader("Remote-Name") ?: username
        return ReaderPrincipal(readerVisit.visit(username, email, displayName), authorities)
    }

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest): Any = "N/A"
}

@Configuration
class SecurityConfig {
    @Bean
    fun authenticationManager(): AuthenticationManager {
        val provider = PreAuthenticatedAuthenticationProvider()
        provider.setPreAuthenticatedUserDetailsService { token ->
            token.principal as ReaderPrincipal
        }
        return ProviderManager(provider)
    }

    @Bean
    fun filterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        readerVisit: ReaderVisit,
    ): SecurityFilterChain {
        val filter = RemoteHeaderAuthenticationFilter(readerVisit)
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
