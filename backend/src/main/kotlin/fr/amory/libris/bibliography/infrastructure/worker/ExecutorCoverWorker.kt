package fr.amory.libris.bibliography.infrastructure.worker

import fr.amory.libris.bibliography.application.cover.CoverWorker
import fr.amory.libris.bibliography.application.cover.FetchAwaitedCovers
import org.springframework.stereotype.Component
import java.util.concurrent.Executor

@Component
class ExecutorCoverWorker(
  private val fetchAwaitedCovers: FetchAwaitedCovers,
  private val executor: Executor) : CoverWorker {
  override fun wake() {
    executor.execute { fetchAwaitedCovers() }
  }
}
