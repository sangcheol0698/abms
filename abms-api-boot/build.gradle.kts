plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    implementation(project(":abms-adapter-web"))
    implementation(project(":abms-adapter-persistence"))
    implementation(project(":abms-adapter-integration"))

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

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
}
