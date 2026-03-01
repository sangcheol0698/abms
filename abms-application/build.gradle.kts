plugins {
    `java-library`
}

dependencies {
    implementation(project(":abms-domain"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("io.projectreactor:reactor-core")
}
