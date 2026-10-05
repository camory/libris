package fr.amory.libris.bibliography.infrastructure.worker

import fr.amory.libris.bibliography.application.cover.FetchAwaitedCovers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.Thread.ofVirtual
import java.util.concurrent.Executors.newSingleThreadExecutor

@Configuration
class WorkerConfig {
  @Bean
  fun executorCoverWorker(fetchAwaitedCovers: FetchAwaitedCovers) =
    ExecutorCoverWorker(fetchAwaitedCovers, newSingleThreadExecutor(ofVirtual().factory()))
}
