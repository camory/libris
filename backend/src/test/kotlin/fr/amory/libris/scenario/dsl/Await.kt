package fr.amory.libris.scenario.dsl

import kotlin.time.Duration.Companion.seconds

private const val POLL_MILLIS = 100L
private val PATIENCE = 5.seconds

fun await(condition: () -> Boolean) {
  val deadline = System.nanoTime() + PATIENCE.inWholeNanoseconds
  while (!condition()) {
    check(System.nanoTime() < deadline) { "still not true after $PATIENCE" }
    Thread.sleep(POLL_MILLIS)
  }
}
