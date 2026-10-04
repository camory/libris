package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.application.cover.CoverWorker

class CoverWorkerObserving<S>(private val observe: () -> S) : CoverWorker {
  private val observed = mutableListOf<S>()

  val wakings: List<S> get() = observed.toList()

  override fun wake() {
    observed += observe()
  }
}
