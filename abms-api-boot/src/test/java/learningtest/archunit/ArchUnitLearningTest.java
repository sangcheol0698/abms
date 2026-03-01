package learningtest.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import org.junit.jupiter.api.DisplayName;

@AnalyzeClasses(packages = "learningtest.archunit")
@DisplayName("ArchUnit 학습 테스트")
public class ArchUnitLearningTest {

    /**
     * Application 클래스를 의존하는 클래스는 application, adapter에만 존재해야 한다.
     */
    @ArchTest
    @DisplayName("Application 계층 의존성 규칙")
    void application(JavaClasses classes) {
        classes().that().resideInAPackage("..application..")
                .should().onlyHaveDependentClassesThat().resideInAnyPackage("..application..", "..adapter..")
                .check(classes);
    }

    /**
     * Application 클래스는 adapter의 클래스를 의존하면 안된다.
     */
    @ArchTest
    @DisplayName("Application 계층은 Adapter 계층을 의존하지 않는다")
    void adapter(JavaClasses classes) {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..adapter..")
                .check(classes);
    }

    /**
     * Domain 클래스는 domain, java
     */
    @ArchTest
    @DisplayName("Domain 계층 의존성 규칙")
    void domain(JavaClasses classes) {
        classes().that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage("..domain..", "java..")
                .check(classes);
    }

}
