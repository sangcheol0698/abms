plugins {
    `java-library`
}

dependencies {
    implementation(project(":abms-application"))
    implementation(project(":abms-domain"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:2.0.0")
}
