package fr.amory.libris

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

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
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "kotlin..",
                "org.jetbrains.annotations..",
                "org.springframework.stereotype..",
                "..domain..",
                "..application..",
            )
            .check(libris)
    }

    @ArchTest
    fun `the infrastructure packages do not depend on each other`(libris: JavaClasses) {
        slices()
            .matching("fr.amory.libris.infra.(*)..")
            .should().notDependOnEachOther()
            .check(libris)
    }

    @ArchTest
    fun `a port of the domain is implemented in the infrastructure only`(libris: JavaClasses) {
        classes()
            .that().implement(JavaClass.Predicates.resideInAPackage("..domain.."))
            .should().resideInAPackage("..infra..")
            .check(libris)
    }

    @ArchTest
    fun `the top-level packages are free of cycles`(libris: JavaClasses) {
        slices()
            .matching("fr.amory.libris.(*)..")
            .should().beFreeOfCycles()
            .check(libris)
    }
}
