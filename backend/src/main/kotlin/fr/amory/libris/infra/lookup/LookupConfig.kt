package fr.amory.libris.infra.lookup

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
@EnableConfigurationProperties(SourcesProperties::class)
class LookupConfig {
    @Bean
    @Order(1)
    fun bnfSource(sources: SourcesProperties) = BnfSource(sources.bnfUrl, sources.timeout)

    @Bean
    @Order(2)
    fun openLibrarySource(sources: SourcesProperties) =
        OpenLibrarySource(sources.openLibraryUrl, sources.timeout)
}
