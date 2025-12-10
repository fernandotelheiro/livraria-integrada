package com.livraria.web;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final String baseUrl;

    protected BasePage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    /** Abre a home (aba Livros é a default) */
    public void openHome() {
        driver.get(baseUrl + "/");
        // garante que a seção de Livros está visível
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#tab-livros.show")
        ));
    }

    /** Clica em uma aba do topo pelo texto do botão (Livros, Clientes, Compras, Relatório) */
    protected void clickTab(String label) {
        WebElement tabBtn = driver.findElement(
                By.xpath("//nav//button[normalize-space()='" + label + "']")
        );
        tabBtn.click();
    }

    /** Lê o texto do toast (sucesso/erro) quando ele aparece */
    public String getToastText() {
        By toastLocator = By.id("toast");
        // espera ficar visível (display != none)
        wait.until(driver -> {
            WebElement el = driver.findElement(toastLocator);
            return el.isDisplayed() && !el.getText().isBlank();
        });
        return driver.findElement(toastLocator).getText();
    }
}
