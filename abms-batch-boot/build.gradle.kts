plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    implementation(project(":abms-adapter-batch"))
    implementation(project(":abms-adapter-persistence"))
    implementation(project(":abms-adapter-integration"))

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-batch")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation(project(":abms-application"))
    testImplementation(project(":abms-domain"))
    testImplementation(project(":abms-adapter-persistence"))
}
