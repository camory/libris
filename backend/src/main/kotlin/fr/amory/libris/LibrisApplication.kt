package fr.amory.libris

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibrisApplication

@Suppress("SpreadOperator") // the Spring Boot entry point idiom; one copy of argv at startup
fun main(args: Array<String>) {
    runApplication<LibrisApplication>(*args)
}
