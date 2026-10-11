package fr.amory.libris.scenario.dsl

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.EntityExchangeResult
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Picture(val format: MediaType?, val bytes: ByteArray, val keptFor: Duration?, val immutable: Boolean) {
  val size: Size? by lazy {
    ImageIO.read(ByteArrayInputStream(bytes))?.let { Size(it.width, it.height) }
  }

  companion object {
    fun of(result: EntityExchangeResult<ByteArray>): Picture {
      val cacheControl = result.responseHeaders.cacheControl.orEmpty().split(",").map { it.trim() }
      return Picture(
        format = result.responseHeaders.contentType,
        bytes = result.responseBody ?: ByteArray(0),
        keptFor = cacheControl.firstOrNull { it.startsWith("max-age=") }?.substringAfter("=")?.toLong()?.seconds,
        immutable = "immutable" in cacheControl,
      )
    }
  }
}

data class Size(val width: Int, val height: Int)

fun Reader.views(address: String): Answer<Picture> =
  libris.view(this, address)
