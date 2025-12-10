package com.livraria.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class ClientesPage extends BasePage {

    // Locators dos elementos da aba "Clientes"
    private final By inputNomeNovo  = By.id("c-nome");
    private final By inputEmailNovo = By.id("c-email");
    private final By btnCriar       = By.xpath("//section[@id='tab-clientes']//button[contains(., 'Criar cliente')]");
    private final By tabelaClientes = By.cssSelector("#tbl-clientes tbody tr");

    public ClientesPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    /** Abre a home, muda para a aba Clientes e espera a tabela carregar */
    public ClientesPage open() {
        openHome();
        clickTab("Clientes");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("tbl-clientes")
        ));
        return this;
    }

    /** Preenche o formulário de criação e clica em "Criar cliente" */
    public ClientesPage criarCliente(String nome, String email) {
        WebElement n = driver.findElement(inputNomeNovo);
        WebElement e = driver.findElement(inputEmailNovo);

        n.clear();
        n.sendKeys(nome);

        e.clear();
        e.sendKeys(email);

        driver.findElement(btnCriar).click();
        return this;
    }

    /** Verifica se existe uma linha na tabela com o nome informado (coluna 2) */
    public boolean existeClienteComNome(String nomeEsperado) {
        List<WebElement> linhas = driver.findElements(tabelaClientes);
        for (WebElement tr : linhas) {
            List<WebElement> tds = tr.findElements(By.tagName("td"));
            if (tds.size() >= 2) {
                String nome = tds.get(1).getText().trim();
                if (nome.equals(nomeEsperado.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
