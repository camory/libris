package fr.amory.libris

import dev.contracteer.verifier.junit.ContracteerServerPort
import dev.contracteer.verifier.junit.ContracteerTest
import jakarta.servlet.Filter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.context.ActiveProfiles

private val FIXED_READER = mapOf(
    "remote-user" to "contracteer",
    "remote-name" to "Contracteer",
    "remote-email" to "contracteer@amory.fr",
    "remote-groups" to "family",
)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("contract-test")
class ApiContractTest @Autowired constructor(
    @field:ContracteerServerPort @param:LocalServerPort val serverPort: Int,
    private val jdbcClient: JdbcClient,
) {
    @ContracteerTest(openApiDoc = "../api/openapi.yaml")
    fun `the API matches the contract`() {
        jdbcClient.sql("truncate table reader").update()
    }

    @TestConfiguration
    @Profile("contract-test")
    class FixedReaderHeaders {
        @Bean
        fun fixedReaderFilter(): FilterRegistrationBean<Filter> {
            val registration = FilterRegistrationBean<Filter>(
                Filter { request, response, chain ->
                    chain.doFilter(FixedReaderRequest(request as HttpServletRequest), response)
                },
            )
            registration.order = Ordered.HIGHEST_PRECEDENCE
            return registration
        }
    }

    private class FixedReaderRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
        override fun getHeader(name: String): String? =
            FIXED_READER[name.lowercase()] ?: super.getHeader(name)
    }
}
