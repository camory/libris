package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import fr.amory.libris.fixture.FreshSchema
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistrar

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.flyway.clean-disabled=false"],
)
@AutoConfigureRestTestClient
@Import(StubbedSources::class)
@ExtendWith(FreshSchema::class)
annotation class ScenarioTest

@TestConfiguration(proxyBeanMethods = false)
class StubbedSources {
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun bnf(): WireMockServer = WireMockServer(options().dynamicPort())

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun openLibrary(): WireMockServer = WireMockServer(options().dynamicPort())

    @Bean
    fun sourceVariables(bnf: WireMockServer, openLibrary: WireMockServer) = DynamicPropertyRegistrar { registry ->
        registry.add("LIBRIS_BNF_URL") { bnf.baseUrl() + "/api/SRU" }
        registry.add("LIBRIS_OPEN_LIBRARY_URL") { openLibrary.baseUrl() }
        registry.add("LIBRIS_SOURCE_TIMEOUT") { "1s" }
    }
}
