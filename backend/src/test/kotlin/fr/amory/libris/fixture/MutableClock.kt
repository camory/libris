package fr.amory.libris.fixture

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset.UTC

class MutableClock(
  private var now: Instant = Instant.parse("2026-09-30T08:00:00Z"),
  private val zone: ZoneId = UTC,
) : Clock() {
  override fun getZone(): ZoneId = zone

  override fun withZone(zone: ZoneId): Clock = MutableClock(now, zone)

  override fun instant(): Instant = now

  fun advance(by: Duration) {
    now = now.plus(by)
  }
}
