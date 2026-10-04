package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.cover.Cover
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.time.Duration

internal fun sourceRestClient(baseUrl: String, timeout: Duration): RestClient {
  val client = HttpClient
    .newBuilder()
    .followRedirects(NORMAL)
    .connectTimeout(timeout)
    .build()
  return RestClient
    .builder()
    .baseUrl(baseUrl)
    .requestFactory(JdkClientHttpRequestFactory(client).apply { setReadTimeout(timeout) })
    .build()
}

internal fun <T> nullOnFailure(read: () -> T?): T? =
  try {
    read()
  } catch (ignored: Exception) {
    null
  }

internal fun RestClient.pictureAt(address: String): Cover? {
  val answer = this
    .get()
    .uri(address)
    .retrieve()
    .toEntity<ByteArray>()
  return answer.body?.let { bytes -> answer.headers.contentType?.let { Cover.of(it.toString(), bytes) } }
}
