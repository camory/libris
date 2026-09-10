package fr.amory.libris.infra.web

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.ComponentScan

@SpringBootConfiguration
@TestComponent
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@ComponentScan
class WebSlice
