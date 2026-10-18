package fr.amory.libris.bibliography.domain.series

import com.fasterxml.uuid.Generators
import java.util.UUID

@JvmInline
value class SeriesId(val value: UUID) {
  companion object {
    fun new(): SeriesId =
      SeriesId(Generators.timeBasedEpochGenerator().generate())
  }
}
