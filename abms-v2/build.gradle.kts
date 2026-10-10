plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("gg.jte.gradle") version "3.2.4"
}

group = "kr.co.abacus"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

val springAiVersion = "2.0.1"
val jteVersion = "3.2.4"

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

dependencies {
    // Web / View
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("gg.jte:jte:$jteVersion")
    implementation("gg.jte:jte-spring-boot-starter-4:$jteVersion")
    implementation("org.webjars:webjars-locator-lite")
    implementation("org.webjars.npm:htmx.org:2.0.11")
    implementation("org.webjars.npm:chart.js:4.5.1")
    implementation("org.webjars.npm:marked:18.0.14")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Persistence
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")

    // AI
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Ops
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.jspecify:jspecify:1.0.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    developmentOnly("com.julien-dubois.bootui:bootui-spring-boot-starter:1.21.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-mysql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

jte {
    // 템플릿을 Java 소스로 생성해 컴파일 시점에 타입 오류를 잡는다.
    generate()
    binaryStaticContent = true
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// ---------------------------------------------------------------------------
// Tailwind CSS
// src/main/tailwind/app.css -> src/main/resources/static/css/app.css
// ---------------------------------------------------------------------------
val npmCommand = if (System.getProperty("os.name").lowercase().contains("windows")) "npm.cmd" else "npm"

val npmInstall by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Installs Tailwind CSS CLI"
    inputs.file("package.json")
    outputs.dir("node_modules")
    commandLine(npmCommand, "install", "--no-audit", "--no-fund")
}

val tailwindBuild by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Builds Tailwind CSS"
    dependsOn(npmInstall)
    inputs.dir("src/main/tailwind")
    inputs.dir("src/main/jte")
    outputs.file("src/main/resources/static/css/app.css")
    commandLine(npmCommand, "run", "build:css")
}

tasks.named("processResources") {
    if (!project.hasProperty("skipTailwind")) {
        dependsOn(tailwindBuild)
    }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName = "abms.jar"
}

tasks.named("jar") {
    enabled = false
}
