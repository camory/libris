package fr.amory.libris.infra.web

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.ComponentScan

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(
    classes = [WebSliceConfiguration::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureRestTestClient
annotation class WebSliceTest

@SpringBootConfiguration
@TestComponent
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@ComponentScan
class WebSliceConfiguration
