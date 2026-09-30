package fr.amory.libris.bibliography.infrastructure.persistence

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path

@ConfigurationProperties("libris.covers")
data class CoversProperties(val dir: Path)
