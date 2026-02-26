import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("net.ltgt.errorprone") version "4.3.0"
}

group = "kr.co.abacus"
version = "0.0.2-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["snippetsDir"] = file("build/generated-snippets")
val springAiVersion by extra("2.0.0-M2")

var mockitoAgent: Configuration = configurations.create("mockitoAgent")
val p6spyVersion by extra("2.0.0")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-security")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.0")
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:7.0:jakarta")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:${p6spyVersion}")

    implementation("org.apache.poi:poi:5.4.1")
    implementation("org.apache.poi:poi-ooxml:5.4.1")

    implementation("com.github.ben-manes.caffeine:caffeine")

    compileOnly("org.jspecify:jspecify:1.0.0")
    errorprone("com.google.errorprone:error_prone_core:2.42.0")
    errorprone("com.uber.nullaway:nullaway:0.12.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    mockitoAgent("org.mockito:mockito-core:5.18.0") { isTransitive = false }
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone  {
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

tasks.withType<Test> {
    useJUnitPlatform()
}
