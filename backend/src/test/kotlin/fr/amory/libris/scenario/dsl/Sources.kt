package fr.amory.libris.scenario.dsl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl

class Sources(private val servers: List<WireMockServer>) {
  fun forgetRequests() {
    servers.forEach { it.resetRequests() }
  }

  fun wereNotAsked() {
    servers.forEach { it.verify(0, anyRequestedFor(anyUrl())) }
  }
}
