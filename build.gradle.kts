import net.ltgt.gradle.errorprone.errorprone
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    base
    id("org.springframework.boot") version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("net.ltgt.errorprone") version "4.3.0" apply false
    java
}

group = "kr.co.abacus"
version = "0.0.2-SNAPSHOT"

val springAiVersion = "2.0.0-M2"

tasks.named("jar") {
    enabled = false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "net.ltgt.errorprone")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    extensions.configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.2")
            mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
        }
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    val mockitoAgent by configurations.creating

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        compileOnly("org.jspecify:jspecify:1.0.0")

        add("errorprone", "com.google.errorprone:error_prone_core:2.42.0")
        add("errorprone", "com.uber.nullaway:nullaway:0.12.3")

        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.assertj:assertj-core")
        testImplementation("org.springframework:spring-test")
        testImplementation("org.mockito:mockito-core:5.18.0")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        mockitoAgent("org.mockito:mockito-core:5.18.0") { isTransitive = false }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-parameters")

        options.errorprone {
            disableAllChecks = true
            excludedPaths.set(".*/build/generated/.*")
            option("NullAway:OnlyNullMarked", "true")
            error("NullAway")
            option("NullAway:JSpecifyMode", "true")
            option("NullAway:ExcludedFieldAnnotations", "jakarta.persistence.*")
        }

        if (name.contains("test", ignoreCase = true)) {
            options.errorprone {
                disable("NullAway")
            }
        }
    }

    plugins.withId("org.springframework.boot") {
        tasks.named("jar") {
            enabled = false
        }
    }
}

tasks.register("bootRun") {
    group = "application"
    description = "Runs API application module"
    dependsOn(":abms-api-boot:bootRun")
}

tasks.register("bootRunBatch") {
    group = "application"
    description = "Runs batch application module"
    dependsOn(":abms-batch-boot:bootRun")
}

tasks.named("build") {
    dependsOn(subprojects.map { "${it.path}:build" })
}

tasks.named("test") {
    dependsOn(subprojects.map { "${it.path}:test" })
}
