package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.cover.Cover
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.time.Duration

private const val PICTURE_LIMIT = 5 * 1024 * 1024

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
  } catch (_: Exception) {
    null
  }

internal fun RestClient.pictureAt(address: String): Cover? =
  this
    .get()
    .uri(address)
    .exchange { _, response -> pictureOf(response) }

private fun pictureOf(response: ClientHttpResponse): Cover? {
  if (!response.statusCode.is2xxSuccessful) return null
  val bytes = response.body.readNBytes(PICTURE_LIMIT + 1)
  return bytes
    .takeIf { it.isNotEmpty() && it.size <= PICTURE_LIMIT }
    ?.let { picture -> response.headers.contentType?.let { Cover.of(it.toString(), picture) } }
}
