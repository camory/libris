package fr.amory.libris

import dev.contracteer.verifier.junit.ContracteerServerPort
import dev.contracteer.verifier.junit.ContracteerTest
import fr.amory.libris.application.ReaderVisit
import fr.amory.libris.domain.Reader
import fr.amory.libris.infra.web.WebSlice
import jakarta.servlet.Filter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.test.context.bean.override.mockito.MockitoBean

private val FIXED_READER = mapOf(
    "remote-user" to "contracteer",
    "remote-name" to "Contracteer",
    "remote-email" to "contracteer@amory.fr",
    "remote-groups" to "family",
)

@SpringBootTest(
    classes = [WebSlice::class, ApiContractTest.FixedReaderHeaders::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@MockitoBean(types = [ReaderVisit::class])
class ApiContractTest @Autowired constructor(
    @field:ContracteerServerPort @param:LocalServerPort val serverPort: Int,
    private val visit: ReaderVisit,
) {
    @ContracteerTest(openApiDoc = "../api/openapi.yaml")
    fun `the API matches the contract`() {
        given(visit.visit("contracteer", "contracteer@amory.fr", "Contracteer"))
            .willReturn(Reader(username = "contracteer", email = "contracteer@amory.fr", displayName = "Contracteer"))
    }

    @TestConfiguration
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
