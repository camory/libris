package fr.amory.libris

import com.tngtech.archunit.base.DescribedPredicate.describe
import com.tngtech.archunit.core.domain.JavaCall
import com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.springframework.data.repository.CrudRepository

/** The six rules of `docs/ARCHITECTURE.md` D02, in D02's order. */
@AnalyzeClasses(packages = ["fr.amory.libris"], importOptions = [ImportOption.DoNotIncludeTests::class])
class ArchitectureTest {

    @ArchTest
    val rule1DomainIsFrameworkFree: ArchRule = classes()
        .that().resideInAPackage("..domain..")
        .should().onlyDependOnClassesThat().resideInAnyPackage(
            "java..",
            "kotlin..",
            "..domain..",
            "org.springframework.data.annotation..",
            "org.springframework.data.relational.core.mapping..",
        )
        .`as`("D02 rule 1: domain depends only on the Kotlin/Java standard libraries and the Spring Data mapping annotations")

    @ArchTest
    val rule2ApplicationDependsOnDomainOnly: ArchRule = classes()
        .that().resideInAPackage("..application..")
        .should().onlyDependOnClassesThat().resideInAnyPackage(
            "java..",
            "kotlin..",
            "..domain..",
            "..application..",
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.annotation..",
        )
        .`as`("D02 rule 2: application depends only on domain (plus @Service / @Transactional)")

    @ArchTest
    val rule3InfraPackagesAreIndependent: ArchRule = slices()
        .matching("fr.amory.libris.infra.(*)..")
        .should().notDependOnEachOther()
        .`as`("D02 rule 3: infra.* packages depend on domain and application, never on each other")

    @ArchTest
    val rule4NoCyclesBetweenTopLevelPackages: ArchRule = slices()
        .matching("fr.amory.libris.(*)..")
        .should().beFreeOfCycles()
        .`as`("D02 rule 4: no cycles between top-level packages")

    @ArchTest
    val rule5PortsAreImplementedInInfraOnly: ArchRule = noClasses()
        .that().resideOutsideOfPackages("..infra..", "..domain..")
        .should().implement(resideInAPackage("..domain.."))
        .`as`("D02 rule 5: ports declared in domain are implemented only in infra")

    @ArchTest
    val rule6CrudRepositorySaveIsNeverCalled: ArchRule = noClasses()
        .should().callMethodWhere(
            describe("save on a CrudRepository") { call: JavaCall<*> ->
                call.target.name == "save" && assignableTo(CrudRepository::class.java).test(call.targetOwner)
            },
        )
        .`as`("D02 rule 6: CrudRepository.save is never called (D11: insert and update through JdbcAggregateTemplate)")
}
