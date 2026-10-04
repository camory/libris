package fr.amory.libris.bibliography.infrastructure.lookup

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SourcesProperties::class)
class LookupConfig {
  @Bean
  fun bnfEditionLookup(sources: SourcesProperties) =
    BnfEditionLookup(sources.bnfUrl, sources.timeout)

  @Bean
  fun openLibrarySource(sources: SourcesProperties) =
    OpenLibrarySource(sources.openLibraryUrl, sources.timeout)

  @Bean
  fun inventaireSource(sources: SourcesProperties) =
    InventaireSource(sources.inventaireUrl, sources.timeout)
}
