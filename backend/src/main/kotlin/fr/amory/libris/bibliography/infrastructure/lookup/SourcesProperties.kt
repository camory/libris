package fr.amory.libris.bibliography.infrastructure.lookup

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("libris.sources")
data class SourcesProperties(
    val bnfUrl: String,
    val openLibraryUrl: String,
    val timeout: Duration,
)
