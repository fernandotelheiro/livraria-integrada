package com.livraria.web;

import com.livraria.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

public abstract class BaseWebTest {

    protected static WebDriver driver;
    protected static final String BASE_URL = "http://localhost:7000";

    @BeforeAll
    static void setupClass() {
        // Se o chromedriver NÃO estiver no PATH, descomenta e aponta pro executável:

        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().setSize(new Dimension(1280, 800));
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void resetData() {
        // Garante estado previsível nos CSVs antes de cada teste
        TestData.resetarLivros();
        TestData.resetarClientes();
    }
}
