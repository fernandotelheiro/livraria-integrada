package com.livraria.support.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TicketUiNetworkFailureTest {

    private WebDriver driver;

    @BeforeEach
    void setup() {
        // Usa o mesmo driver que já usa em TicketUiTest
        driver = new ChromeDriver();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Simula "servidor fora do ar".
     * O fetch para "/api/tickets" falha e o JS deve mostrar
     * "Erro de rede ao carregar tickets." na div #msg.
     */
    @Test
    void deveMostrarErroDeRedeQuandoServidorForaDoAr() {
        // Path para o support-index.html gerado no build/resources/main/public (projeto Gradle)
        String fileUrl = Paths
                .get("build", "resources", "main", "public", "support-index.html")
                .toAbsolutePath()
                .toUri()
                .toString();

        driver.get(fileUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement msg = wait.until(d -> {
            var els = d.findElements(By.id("msg"));
            if (els.isEmpty()) {
                // ainda não apareceu, continua esperando
                return null;
            }
            var el = els.get(0);
            String text = el.getText();
            // Só consideramos "ok" quando a mensagem de erro de rede aparecer
            return text != null && text.contains("Erro de rede ao carregar tickets.")
                    ? el
                    : null;
        });

        assertTrue(
                msg.getText().contains("Erro de rede ao carregar tickets."),
                "Mensagem de erro de rede não apareceu como esperado"
        );
    }
}
