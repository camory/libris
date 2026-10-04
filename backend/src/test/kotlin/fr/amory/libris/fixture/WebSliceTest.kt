package fr.amory.libris.fixture

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.test.http.server.LocalTestWebServer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.test.web.servlet.client.RestTestClient

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
@ComponentScan("fr.amory.libris.web")
class WebSliceConfiguration {
  @Bean
  fun restTestClient(context: ApplicationContext): RestTestClient =
    RestTestClient.bindToServer(SimpleClientHttpRequestFactory())
      .uriBuilderFactory(LocalTestWebServer.obtain(context).uriBuilderFactory())
      .build()
}
