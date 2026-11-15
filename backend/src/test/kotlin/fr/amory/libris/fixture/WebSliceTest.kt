package fr.amory.libris.fixture

import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.bibliography.application.edition.ListAuthorNames
import fr.amory.libris.bibliography.application.series.ListSeriesNames
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.AddMemberToBookshelf
import fr.amory.libris.library.application.ChooseDefaultBookshelf
import fr.amory.libris.library.application.CreateBookshelf
import fr.amory.libris.library.application.FindBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.ListReaderBookshelves
import fr.amory.libris.library.application.ListReaders
import fr.amory.libris.library.application.RemoveCopy
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.catalogue.BrowseCatalogue
import fr.amory.libris.library.application.catalogue.FindCatalogueEdition
import fr.amory.libris.library.application.catalogue.FindCatalogueSeries
import fr.amory.libris.library.application.catalogue.ListCatalogueSeries
import fr.amory.libris.library.application.catalogue.SaveEdition
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.client.RestTestClient

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(
  classes = [WebSliceConfiguration::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureRestTestClient
@MockitoBean(
  types = [
    WelcomeReader::class,
    LookupIsbnForReader::class,
    FindDefaultBookshelf::class,
    AddBookToBookshelf::class,
    BrowseCatalogue::class,
    FindCover::class,
    ListReaderBookshelves::class,
    CreateBookshelf::class,
    FindBookshelf::class,
    ListReaders::class,
    AddMemberToBookshelf::class,
    ChooseDefaultBookshelf::class,
    ListCatalogueSeries::class,
    FindCatalogueSeries::class,
    FindCatalogueEdition::class,
    ListSeriesNames::class,
    ListAuthorNames::class,
    RemoveCopy::class,
    SaveEdition::class,
  ],
)
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
