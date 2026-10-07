package fr.amory.libris

import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.type
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.springframework.transaction.TransactionStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

@AnalyzeClasses(
  packages = ["fr.amory.libris"],
  importOptions = [ImportOption.DoNotIncludeTests::class],
)
class ArchitectureTest {
  @ArchTest
  fun `the domain depends on the standard libraries and the uuid generator only`(libris: JavaClasses) {
    classes()
      .that().resideInAPackage("..domain..")
      .should().onlyDependOnClassesThat()
      .resideInAnyPackage(
        "java..",
        "javax.imageio..",
        "kotlin..",
        "org.jetbrains.annotations..",
        "com.fasterxml.uuid..",
        "..domain..",
      )
      .check(libris)
  }

  @ArchTest
  fun `the application depends on the domain only`(libris: JavaClasses) {
    classes()
      .that().resideInAPackage("..application..")
      .should().onlyDependOnClassesThat(
        resideInAnyPackage(
          "java..",
          "kotlin..",
          "org.jetbrains.annotations..",
          "org.springframework.stereotype..",
          "org.springframework.transaction.support..",
          "io.github.oshai.kotlinlogging..",
          "..domain..",
          "..application..",
        ).or(type(TransactionStatus::class.java)),
      )
      .check(libris)
  }

  @ArchTest
  fun `the infrastructure packages never depend on each other`(libris: JavaClasses) {
    slices()
      .matching("fr.amory.libris.(*).infrastructure.(*)..")
      .should().notDependOnEachOther()
      .check(libris)
  }

  @ArchTest
  fun `the controllers sit in the web`(libris: JavaClasses) {
    classes()
      .that().areAnnotatedWith(RestController::class.java)
      .or().areAnnotatedWith(RestControllerAdvice::class.java)
      .should().resideInAPackage("fr.amory.libris.web..")
      .check(libris)
  }

  @ArchTest
  fun `the contexts know nothing of the web`(libris: JavaClasses) {
    noClasses()
      .that().resideInAnyPackage("fr.amory.libris.bibliography..", "fr.amory.libris.library..")
      .should().dependOnClassesThat().resideInAPackage("fr.amory.libris.web..")
      .check(libris)
  }

  @ArchTest
  fun `a port of the domain is implemented in the infrastructure only`(libris: JavaClasses) {
    classes()
      .that().implement(resideInAPackage("..domain.."))
      .should().resideInAPackage("..infrastructure..")
      .check(libris)
  }

  @ArchTest
  fun `the contexts are free of cycles`(libris: JavaClasses) {
    slices()
      .matching("fr.amory.libris.(*)..")
      .should().beFreeOfCycles()
      .check(libris)
  }

  @ArchTest
  fun `the bibliography knows nothing of the library`(libris: JavaClasses) {
    noClasses()
      .that().resideInAPackage("..bibliography..")
      .should().dependOnClassesThat().resideInAPackage("..library..")
      .check(libris)
  }
}
