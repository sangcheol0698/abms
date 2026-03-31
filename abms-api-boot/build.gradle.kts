import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("org.springframework.boot")
    id("org.asciidoctor.jvm.convert") version "4.0.2"
}

val snippetsDir = file("build/generated-snippets")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation(project(":abms-adapter-web"))
    implementation(project(":abms-adapter-persistence"))
    implementation(project(":abms-adapter-integration"))

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:8.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation(project(":abms-application"))
    testImplementation(project(":abms-domain"))
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-mail")
    testImplementation("org.apache.poi:poi:5.4.1")
    testImplementation("org.apache.poi:poi-ooxml:5.4.1")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("io.micrometer:micrometer-registry-prometheus")
    testImplementation("io.projectreactor:reactor-core")
}

tasks.named<Test>("test") {
    outputs.dir(snippetsDir)
}

tasks.named<AsciidoctorTask>("asciidoctor") {
    inputs.dir(snippetsDir)
    dependsOn(tasks.named("test"))
    attributes(
            mapOf(
                    "snippets" to snippetsDir
            )
    )
}

val syncDocsForBootRun by tasks.registering(Sync::class) {
    dependsOn(tasks.named("asciidoctor"))
    from(layout.buildDirectory.dir("docs/asciidoc")) {
        into("static/docs")
    }
    into(layout.buildDirectory.dir("generated-docs-resources"))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    dependsOn(tasks.named("asciidoctor"))
    from(layout.buildDirectory.dir("docs/asciidoc")) {
        into("static/docs")
    }
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    dependsOn(syncDocsForBootRun)
    classpath(layout.buildDirectory.dir("generated-docs-resources"))
}
