package fr.amory.libris.scenario.dsl

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.EntityExchangeResult
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Picture(val format: MediaType?, val bytes: ByteArray, val keptFor: Duration?, val immutable: Boolean)

fun Reader.views(address: String): Answer<Picture> =
  libris.view(this, address)

fun pictureOf(result: EntityExchangeResult<ByteArray>): Picture {
  val cacheControl = result.responseHeaders.cacheControl.orEmpty().split(",").map { it.trim() }
  return Picture(
    format = result.responseHeaders.contentType,
    bytes = result.responseBody ?: ByteArray(0),
    keptFor = cacheControl.firstOrNull { it.startsWith("max-age=") }?.substringAfter("=")?.toLong()?.seconds,
    immutable = "immutable" in cacheControl,
  )
}
