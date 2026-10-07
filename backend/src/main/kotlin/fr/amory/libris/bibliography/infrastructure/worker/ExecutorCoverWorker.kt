package fr.amory.libris.bibliography.infrastructure.worker

import fr.amory.libris.bibliography.application.cover.CoverWorker
import fr.amory.libris.bibliography.application.cover.FetchAwaitedCovers
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executor

class ExecutorCoverWorker(
  private val fetchAwaitedCovers: FetchAwaitedCovers,
  private val executor: Executor) : CoverWorker {
  private val logger = KotlinLogging.logger {}

  @Scheduled(initialDelay = 0, fixedDelayString = "\${libris.worker.every}")
  override fun wake() {
    executor.execute { run() }
  }

  private fun run() {
    logger.info { "Fetching the awaited covers" }
    fetchAwaitedCovers()
  }
}
