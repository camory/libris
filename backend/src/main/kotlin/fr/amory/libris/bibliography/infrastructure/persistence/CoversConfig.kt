package fr.amory.libris.bibliography.infrastructure.persistence

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CoversProperties::class)
class CoversConfig {
  @Bean
  fun fileCoverStore(covers: CoversProperties) = FileCoverStore(covers.directory)
}
