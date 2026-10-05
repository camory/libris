package fr.amory.libris.bibliography.infrastructure.lookup

import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

private const val MARCXCHANGE = "info:lc/xmlns/marcxchange-v2"

internal class UnimarcRecord(
  private val controls: List<Pair<String, String>>,
  private val fields: List<UnimarcField>) {
  fun control(tag: String): String? =
    controls.firstOrNull { it.first == tag }?.second

  fun value(tag: String, code: String): String? =
    field(tag)?.value(code)

  fun field(tag: String): UnimarcField? =
    fields.firstOrNull { it.tag == tag }

  fun field(tag: String, indicator2: String): UnimarcField? =
    fields.firstOrNull { it.tag == tag && it.indicator2 == indicator2 }

  fun fields(tags: Set<String>): List<UnimarcField> =
    fields.filter { it.tag in tags }

  companion object {
    fun parse(xml: String): UnimarcRecord? {
      val factory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
      }
      val document = factory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
      val record = document.getElementsByTagNameNS(MARCXCHANGE, "record").item(0) as Element?
      return record?.let { UnimarcRecord(controlsOf(it), fieldsOf(it)) }
    }
  }
}

internal class UnimarcField(
  val tag: String,
  val indicator2: String,
  private val subfields: List<Pair<String, String>>) {
  fun value(code: String): String? =
    subfields.firstOrNull { it.first == code }?.second
}

private fun controlsOf(record: Element): List<Pair<String, String>> =
  record.getElementsByTagNameNS(MARCXCHANGE, "controlfield").let { controls ->
    (0 until controls.length).map { index ->
      (controls.item(index) as Element).let { it.getAttribute("tag") to it.textContent }
    }
  }

private fun fieldsOf(record: Element): List<UnimarcField> =
  record.getElementsByTagNameNS(MARCXCHANGE, "datafield").let { fields ->
    (0 until fields.length).map { index -> fieldOf(fields.item(index) as Element) }
  }

private fun fieldOf(field: Element): UnimarcField =
  field.getElementsByTagNameNS(MARCXCHANGE, "subfield").let { subfields ->
    UnimarcField(
      tag = field.getAttribute("tag"),
      indicator2 = field.getAttribute("ind2"),
      subfields = (0 until subfields.length).map { index ->
        (subfields.item(index) as Element).let { it.getAttribute("code") to it.textContent }
      },
    )
  }
