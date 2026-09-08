package fr.amory.libris

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibrisApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<LibrisApplication>(*args)
}
