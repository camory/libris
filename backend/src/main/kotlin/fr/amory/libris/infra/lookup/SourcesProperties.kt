package fr.amory.libris.infra.lookup

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("libris.sources")
data class SourcesProperties(
    val openLibraryUrl: String,
    val timeout: Duration,
)
