plugins {
    `java-library`
}

dependencies {
    implementation(project(":abms-application"))
    implementation(project(":abms-domain"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.0")
    implementation("org.apache.poi:poi:5.4.1")
    implementation("org.apache.poi:poi-ooxml:5.4.1")
}
