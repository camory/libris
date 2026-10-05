package fr.amory.libris.bibliography.infrastructure.lookup

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SourcesProperties::class)
class LookupConfig {
  @Bean
  fun bnfSource(sources: SourcesProperties) =
    BnfSource(sources.bnfUrl, sources.bnfCoversUrl, sources.timeout)

  @Bean
  fun openLibrarySource(sources: SourcesProperties) =
    OpenLibrarySource(sources.openLibraryUrl, sources.openLibraryCoversUrl, sources.timeout)

  @Bean
  fun inventaireSource(sources: SourcesProperties) =
    InventaireSource(sources.inventaireUrl, sources.timeout)
}
