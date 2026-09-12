package fr.amory.libris.infra.lookup

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SourcesProperties::class)
class LookupConfig {
    @Bean
    fun openLibrarySource(sources: SourcesProperties) = OpenLibrarySource(sources.openLibraryUrl, sources.timeout)
}
