package fr.amory.libris.infra.lookup

import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.time.Duration

internal fun sourceRestClient(baseUrl: String, timeout: Duration): RestClient {
    val client = HttpClient.newBuilder()
        .followRedirects(NORMAL)
        .connectTimeout(timeout)
        .build()
    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(JdkClientHttpRequestFactory(client).apply { setReadTimeout(timeout) })
        .build()
}
