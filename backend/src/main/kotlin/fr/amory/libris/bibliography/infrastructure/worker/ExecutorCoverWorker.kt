package fr.amory.libris.bibliography.infrastructure.worker

import fr.amory.libris.bibliography.application.cover.CoverWorker
import fr.amory.libris.bibliography.application.cover.FetchAwaitedCovers
import org.slf4j.LoggerFactory.getLogger
import java.util.concurrent.Executor

class ExecutorCoverWorker(
  private val fetchAwaitedCovers: FetchAwaitedCovers,
  private val executor: Executor) : CoverWorker {
  override fun wake() {
    executor.execute { run() }
  }

  private fun run() {
    log.info("Fetching the awaited covers")
    fetchAwaitedCovers()
  }

  private companion object {
    val log = getLogger(ExecutorCoverWorker::class.java)
  }
}
