package fr.amory.libris.infra.web

import fr.amory.libris.application.ReaderVisit
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.filter.OncePerRequestFilter

private const val ADMIN_GROUP = "libris-admin"

const val READER_AUTHORITY = "ROLE_READER"
const val ADMIN_AUTHORITY = "ROLE_ADMIN"

class RemoteHeaderAuthenticationFilter(private val readerVisit: ReaderVisit) : OncePerRequestFilter() {
    override fun shouldNotFilterErrorDispatch(): Boolean = false

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        authenticationOf(request)?.let { SecurityContextHolder.getContext().authentication = it }
        filterChain.doFilter(request, response)
    }

    private fun authenticationOf(request: HttpServletRequest): PreAuthenticatedAuthenticationToken? {
        val username = request.getHeader("Remote-User")
        val email = request.getHeader("Remote-Email")?.takeUnless { it.isBlank() }
        if (username == null || email == null) return null
        val groups = request.getHeader("Remote-Groups").orEmpty().split(",").map { it.trim() }
        val authorities = buildList {
            add(SimpleGrantedAuthority(READER_AUTHORITY))
            if (ADMIN_GROUP in groups) add(SimpleGrantedAuthority(ADMIN_AUTHORITY))
        }
        val reader = readerVisit.visit(username, email, request.getHeader("Remote-Name") ?: username)
        return PreAuthenticatedAuthenticationToken(reader, "N/A", authorities)
    }
}

@Configuration
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity, readerVisit: ReaderVisit): SecurityFilterChain {
        val unsafeWrite = RequestMatcher {
            it.method != HttpMethod.GET.name() && it.getHeader("X-Requested-With") == null
        }
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(
                RemoteHeaderAuthenticationFilter(readerVisit),
                UsernamePasswordAuthenticationFilter::class.java,
            )
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers(unsafeWrite).denyAll()
                it.anyRequest().authenticated()
            }
            .build()
    }
}
