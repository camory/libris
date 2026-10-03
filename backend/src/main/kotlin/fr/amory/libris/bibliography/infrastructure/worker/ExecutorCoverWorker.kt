package fr.amory.libris.bibliography.infrastructure.worker

import fr.amory.libris.bibliography.application.cover.CoverWorker
import fr.amory.libris.bibliography.application.cover.FetchAwaitedCovers
import org.springframework.stereotype.Component

@Component
class ExecutorCoverWorker(private val fetchAwaitedCovers: FetchAwaitedCovers) : CoverWorker {
  override fun wake() {
    fetchAwaitedCovers()
  }
}
