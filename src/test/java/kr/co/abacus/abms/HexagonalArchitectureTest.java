package kr.co.abacus.abms;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(packages = "kr.co.abacus.abms", importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonalArchitectureTest {

    @ArchTest
    void hexagonalArchitecture(JavaClasses classes) {
        Architectures.layeredArchitecture()
            .consideringAllDependencies()
            .layer("domain").definedBy("kr.co.abacus.abms.domain..")
            .layer("application").definedBy("kr.co.abacus.abms.application..")
            .layer("adapter").definedBy("kr.co.abacus.abms.adapter..")
            .whereLayer("domain").mayOnlyBeAccessedByLayers("application", "adapter")
            .whereLayer("application").mayOnlyBeAccessedByLayers("adapter")
            .whereLayer("adapter").mayNotBeAccessedByAnyLayer()
            .check(classes);
    }

}
