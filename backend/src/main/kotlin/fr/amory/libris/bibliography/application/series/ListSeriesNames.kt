package fr.amory.libris.bibliography.application.series

import fr.amory.libris.bibliography.domain.series.SeriesRepository
import org.springframework.stereotype.Service

@Service
class ListSeriesNames(private val seriesRepository: SeriesRepository) {
  operator fun invoke(): List<String> =
    seriesRepository.findAll().map { it.name }
}
