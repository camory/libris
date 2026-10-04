package fr.amory.libris.web.security

import fr.amory.libris.library.application.WelcomeReader
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.firewall.StrictHttpFirewall
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.filter.OncePerRequestFilter

class RemoteHeaderAuthenticationFilter(private val welcomeReader: WelcomeReader) : OncePerRequestFilter() {
  override fun shouldNotFilterErrorDispatch(): Boolean =
    false

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    authenticationOf(request)?.let { SecurityContextHolder.getContext().authentication = it }
    filterChain.doFilter(request, response)
  }

  private fun authenticationOf(request: HttpServletRequest): PreAuthenticatedAuthenticationToken? =
    RemoteIdentity.of(request)?.let {
      val principal = welcomeReader(it.username, it.email, it.displayName)
      PreAuthenticatedAuthenticationToken(principal, "N/A", it.authorities)
    }
}

@Configuration
class SecurityConfig {
  @Bean
  fun filterChain(http: HttpSecurity, welcomeReader: WelcomeReader): SecurityFilterChain {
    val unsafeWrite = RequestMatcher {
      it.method != HttpMethod.GET.name() && it.getHeader("X-Requested-With") == null
    }
    return http
      .csrf { it.disable() }
      .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
      .addFilterBefore(
        RemoteHeaderAuthenticationFilter(welcomeReader),
        UsernamePasswordAuthenticationFilter::class.java,
      )
      .authorizeHttpRequests {
        it.requestMatchers("/actuator/**").permitAll()
        it.requestMatchers(unsafeWrite).denyAll()
        it.anyRequest().authenticated()
      }
      .build()
  }

  @Bean
  fun httpFirewall(): StrictHttpFirewall =
    StrictHttpFirewall().apply {
      setAllowedHeaderValues { value -> utf8(value).all { Character.isDefined(it) && !it.isISOControl() } }
    }
}
