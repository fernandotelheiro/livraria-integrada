package com.livraria.web;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class LivrosPage extends BasePage {

    private final By inputTituloNovo = By.id("l-nome");
    private final By inputAutorNovo  = By.id("l-autor");
    private final By inputQtdNovo    = By.id("l-qtd");
    private final By inputPrecoNovo  = By.id("l-preco");

    private final By btnCriar = By.xpath("//section[@id='tab-livros']//button[contains(., 'Criar livro')]");
    private final By tabelaLivros = By.cssSelector("#tbl-livros tbody tr");

    public LivrosPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    public LivrosPage open() {
        openHome(); // já abre na aba Livros
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("tbl-livros")
        ));
        return this;
    }

    public LivrosPage criarLivro(String titulo, String autor, int quantidade, String preco) {
        WebElement t = driver.findElement(inputTituloNovo);
        WebElement a = driver.findElement(inputAutorNovo);
        WebElement q = driver.findElement(inputQtdNovo);
        WebElement p = driver.findElement(inputPrecoNovo);

        t.clear(); t.sendKeys(titulo);
        a.clear(); a.sendKeys(autor);
        q.clear(); q.sendKeys(String.valueOf(quantidade));
        p.clear(); p.sendKeys(preco);

        driver.findElement(btnCriar).click();
        return this;
    }

    /** Verifica se existe uma linha na tabela com o título informado (coluna 2) */
    public boolean existeLivroComTitulo(String tituloEsperado) {
        List<WebElement> linhas = driver.findElements(tabelaLivros);
        for (WebElement tr : linhas) {
            List<WebElement> tds = tr.findElements(By.tagName("td"));
            if (tds.size() >= 2) {
                String titulo = tds.get(1).getText().trim();
                if (titulo.equals(tituloEsperado.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
