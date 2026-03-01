plugins {
    `java-library`
}

dependencies {
    implementation(project(":abms-application"))
    implementation(project(":abms-domain"))

    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
