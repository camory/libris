package fr.amory.libris.fixture

import fr.amory.libris.bibliography.infrastructure.web.IsbnController
import fr.amory.libris.library.infrastructure.web.MeController
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
@ComponentScan(basePackageClasses = [IsbnController::class, MeController::class])
class WebSliceConfiguration
