package fr.amory.libris.bibliography.infrastructure.lookup

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
@EnableConfigurationProperties(SourcesProperties::class)
class LookupConfig {
    @Bean
    @Order(1)
    fun bnfEditionLookup(sources: SourcesProperties) = BnfEditionLookup(sources.bnfUrl, sources.timeout)

    @Bean
    @Order(2)
    fun openLibraryEditionLookup(sources: SourcesProperties) =
        OpenLibraryEditionLookup(sources.openLibraryUrl, sources.timeout)
}
