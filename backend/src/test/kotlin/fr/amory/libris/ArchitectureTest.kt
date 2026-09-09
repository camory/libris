package fr.amory.libris

import com.tngtech.archunit.core.domain.JavaCall
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.properties.HasName
import com.tngtech.archunit.core.domain.properties.HasOwner
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.springframework.data.repository.CrudRepository

@AnalyzeClasses(
    packages = ["fr.amory.libris"],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class ArchitectureTest {
    @ArchTest
    fun `the domain depends on the standard libraries only`(libris: JavaClasses) {
        classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "kotlin..",
                "org.jetbrains.annotations..",
                "org.springframework.data.annotation..",
                "org.springframework.data.relational.core.mapping..",
                "..domain..",
            )
            .check(libris)
    }

    @ArchTest
    fun `the top-level packages are free of cycles`(libris: JavaClasses) {
        slices()
            .matching("fr.amory.libris.(*)..")
            .should().beFreeOfCycles()
            .check(libris)
    }

    @ArchTest
    fun `the infra packages do not depend on each other`(libris: JavaClasses) {
        slices()
            .matching("fr.amory.libris.infra.(*)..")
            .should().notDependOnEachOther()
            .check(libris)
    }

    @ArchTest
    fun `a port declared in the domain is implemented in infra`(libris: JavaClasses) {
        classes()
            .that().implement(JavaClass.Predicates.resideInAPackage("..domain.."))
            .should().resideInAPackage("..infra..")
            .check(libris)
    }

    @ArchTest
    fun `nothing saves through a crud repository`(libris: JavaClasses) {
        val crudRepository = JavaClass.Predicates.assignableTo(CrudRepository::class.java)
        val save =
            JavaCall.Predicates
                .target(HasName.Predicates.name("save"))
                .and(JavaCall.Predicates.target(HasOwner.Predicates.With.owner(crudRepository)))
        noClasses()
            .should().callMethodWhere(save)
            .check(libris)
    }
}
