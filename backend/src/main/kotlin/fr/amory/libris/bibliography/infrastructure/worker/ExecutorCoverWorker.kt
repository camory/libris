package fr.amory.libris.bibliography.infrastructure.worker

import fr.amory.libris.bibliography.application.cover.CoverWorker
import fr.amory.libris.bibliography.application.cover.FetchAwaitedCovers
import java.util.concurrent.Executor

class ExecutorCoverWorker(
  private val fetchAwaitedCovers: FetchAwaitedCovers,
  private val executor: Executor) : CoverWorker {
  override fun wake() {
    executor.execute { fetchAwaitedCovers() }
  }
}
