plugins {
    id("java")
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.microjobs"
version = "1.0.0-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.18")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.7.18")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.7.18")
    implementation("org.springframework.boot:spring-boot-starter-security:2.7.18")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.7.18")
    
    // Database
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.flywaydb:flyway-core:8.5.13")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.18")
    
    // Development
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:2.7.18")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    compileOnly("org.projectlombok:lombok:1.18.30")
}