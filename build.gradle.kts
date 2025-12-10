plugins {
    java
    application
}

group = "com.livraria"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // --- PRODUÇÃO ---

    // Javalin (servidor HTTP)
    implementation("io.javalin:javalin:5.6.3")

    // SLF4J simples (para logs do Javalin)
    implementation("org.slf4j:slf4j-simple:2.0.13")

    // Jackson (ObjectMapper)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")

    // --- TESTE ---

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // Selenium WebDriver
    testImplementation("org.seleniumhq.selenium:selenium-java:4.21.0")

    // WebDriverManager
    testImplementation("io.github.bonigarcia:webdrivermanager:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.livraria.Main")
}
