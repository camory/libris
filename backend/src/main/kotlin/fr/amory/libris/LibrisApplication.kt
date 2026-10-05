package fr.amory.libris

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

@SpringBootApplication
class LibrisApplication {
  @Bean
  fun systemClock(): Clock =
    Clock.systemUTC()
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
  runApplication<LibrisApplication>(*args)
}
