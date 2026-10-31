package fr.amory.libris.scenario

import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.asks
import io.kotest.matchers.shouldBe
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.FieldSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.context.TestPropertySource
import java.util.UUID

@ScenarioTest
class SummaryScenarios @Autowired constructor(libris: Libris, private val bnf: BnfStubs) {
  private val lea = libris.reader("lea", "Léa")

  @ParameterizedTest(name = "S1 The BnF gives a summary, {argumentSetName}")
  @FieldSource("SUMMARIES")
  fun `S1 The BnF gives a summary`(isbn: String, summary: String) {
    // Given
    bnf.knows(isbn)

    // When
    val answer = lea.asks(isbn)

    // Then
    answer.value.summary shouldBe summary
  }

  private companion object {
    @JvmField
    val SUMMARIES = listOf(
      argumentSet("Mont Tombe", "9782344008980", MONT_TOMBE),
      argumentSet("Le Larousse du Pain", "9782035973689", LAROUSSE_DU_PAIN),
    )
  }
}

@ScenarioTest
@TestPropertySource(properties = ["spring.flyway.target=12"])
class StoredSummaryScenarios @Autowired constructor(
  private val flyway: Flyway,
  private val db: JdbcClient,
  private val bnf: BnfStubs) {

  @Test
  fun `S4 Stored editions gain their summary, the BnF gives one`() {
    // Given
    val montTombe = editionStored("9782344008980")
    val larousse = editionStored("9782035973689", summary = "Le pain, de la farine à la miche.")
    val withoutIsbn = editionStored(isbn13 = null)
    bnf.knows("9782344008980")
    bnf.knows("9782035973689")

    // When
    librisStartsOnTheRelease()

    // Then
    summaryOf(montTombe) shouldBe MONT_TOMBE
    summaryOf(larousse) shouldBe "Le pain, de la farine à la miche."
    summaryOf(withoutIsbn) shouldBe null
  }

  @Disabled("summary")
  @Test
  fun `S4 Stored editions gain their summary, the BnF has none`() {
    // Given
    val onePiece = editionStored("9782723488525")
    bnf.knows("9782723488525")

    // When
    librisStartsOnTheRelease()

    // Then
    summaryOf(onePiece) shouldBe null
  }

  @Disabled("summary")
  @Test
  fun `S4 Stored editions gain their summary, the BnF fails`() {
    // Given
    val montTombe = editionStored("9782344008980")
    bnf.fails()

    // When
    librisStartsOnTheRelease()

    // Then
    summaryOf(montTombe) shouldBe null
  }

  private fun editionStored(isbn13: String?, summary: String? = null): UUID =
    UUID.randomUUID().also { id ->
      db.sql("INSERT INTO edition (id, isbn13, kind, title, summary) VALUES (:id, :isbn13, 'BOOK', 'Stocké', :summary)")
        .param("id", id)
        .param("isbn13", isbn13)
        .param("summary", summary)
        .update()
    }

  private fun librisStartsOnTheRelease() {
    Flyway.configure().configuration(flyway.configuration).target(MigrationVersion.LATEST).load().migrate()
  }

  private fun summaryOf(edition: UUID): String? =
    db.sql("SELECT summary FROM edition WHERE id = :id")
      .param("id", edition)
      .query { rs, _ -> rs.getString("summary") }
      .list()
      .single()
}

private val MONT_TOMBE = listOf(
  "Le Mont Saint-Michel à l'origine de l'Apocalypse",
  "Depuis des siècles, le Mont Saint-Michel est la source de nombreuses légendes.",
  "Des druides celtes au crâne de l'évêque Aubert en passant par la forêt de Scissy, " +
    "le lieutenant Clotilde Dumont pensait avoir tout entendu en prenant son poste à la gendarmerie du Mont… " +
    "Mais ses idées sont vite chamboulées lorsque, suite à un éboulement, un mystérieux sanctuaire est mis à jour.",
  "D'après certains écrits, le Mont Saint-Michel serait en réalité une prison, et ce sanctuaire, " +
    "une cellule : celle du Dragon de l'Apocalypse, enfermé suite à sa défaite contre l'Archange Michel. " +
    "En violant ce lieu sacré, les archéologues risqueraient alors de précipiter la fin des temps.",
  "Et si tout ceci n'était qu'un leurre ? " +
    "Une belle histoire fabriquée de toutes pièces par des trafiquants d'art, " +
    "désireux de s'approprier quelques reliques précieuses… " +
    "Pour Nicolas Quitin, spécialiste en ésotérisme très controversé, " +
    "tout ça n'est qu'une sombre mascarade et il compte bien venir en aide au lieutenant Dumont.",
  "Pourtant, quand les signes décrits dans l'Apocalypse selon Saint-Jean s'abattent sur le Mont Saint-Michel, " +
    "Clotilde commence à douter…",
  "Thriller esotérique pour adultes, Mont Tombe est une plongée vertigineuse " +
    "dans les légendes les plus obscures du Mont Saint-Michel.",
).joinToString("\n")

private const val LAROUSSE_DU_PAIN =
  "Impossible de résister à l'appel d'un pain tout juste sorti du four, encore chaud et croustillant : " +
    "depuis notre plus tendre enfance, le pain fait partie de notre quotidien. " +
    "Éric Kayser vous livre ses meilleures recettes et ses astuces pour réussir votre propre pain au levain, " +
    "d'une qualité incomparable, et sans recours à la machine à pain. " +
    "DÉCOUVREZ COMMENT PRÉPARER LE LEVAIN, PÉTRIR, FAÇONNER ET CUIRE VOTRE PAIN, " +
    "GRÂCE À 80 RECETTES DÉTAILLÉES ET ILLUSTRÉES ÉTAPE PAR ÉTAPE. " +
    "• LES GRANDS CLASSIQUES : baguette, boule, bâtard, polka, épi... " +
    "• LES PAINS SPÉCIAUX : multigrains, seigle, campagne, sans gluten... " +
    "• LES PAINS BIO AU LEVAIN NATUREL : pain à l'épeautre, tourte de meule... " +
    "• LES PAINS AUX INGRÉDIENTS : pain aux figues, à l'orange, à l'encre de seiche... " +
    "• LES PAINS À L'HUILE : ciabatta, fougasse, pizza... " +
    "• LES VIENNOISERIES ET PAINS SUCRÉS : croissant, pain au chocolat, brioche... " +
    "• LES PETITS PAINS : au pavot, aux lardons et aux noix de pécan, aux algues... " +
    "• LES PAINS RÉGIONAUX : marguerite, vivarais, tabatière... " +
    "• LES PAINS DU MONDE : focaccia, ekmek turc, macatia de La Réunion... " +
    "Issu d'une lignée de boulangers d'origine alsacienne, ÉRIC KAYSER porte en lui la passion du pain. " +
    "Ses pains au levain sont appréciés tant en France qu'à l'étranger – " +
    "en particulier au Japon et aux États-Unis, où il a ouvert de nombreuses boulangeries. " +
    "Ce livre traduit sa volonté de transmettre et de partager son savoir-faire."
