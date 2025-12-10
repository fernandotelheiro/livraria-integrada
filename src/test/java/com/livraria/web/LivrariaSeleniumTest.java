package com.livraria.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LivrariaSeleniumTest extends BaseWebTest {

    @Test
    @DisplayName("Navegar entre abas e validar listagens básicas (Livros / Clientes)")
    void deveNavegarEntreAbasEValidarListas() {
        // RUBRICA 1: automatiza interação + navegação
        var livrosPage = new LivrosPage(driver, BASE_URL).open();
        assertTrue(livrosPage.existeLivroComTitulo("1984"),
                "Deveria listar o livro '1984' vindo do CSV inicial");

        var clientesPage = new ClientesPage(driver, BASE_URL).open();
        assertTrue(clientesPage.existeClienteComNome("Fulano da Silva"),
                "Deveria listar o cliente 'Fulano da Silva' vindo do CSV inicial");
    }

    @ParameterizedTest
    @DisplayName("Criar livros com dados variados (teste parametrizado)")
    @CsvSource({
            // titulo                 , autor          , qtd , preco
            "Livro Teste 1           , Autor A        , 3   , 19,90",
            "Livro Teste 2           , Autor B        , 0   , 0,00",
            "'   Com Espaços   '     , '   Autor C '  , 1   , 5,50"
    })
    void deveCriarLivrosComCenariosDiferentes(String titulo, String autor, int qtd, String preco) {
        // RUBRICA 2: teste parametrizado com Selenium
        var livrosPage = new LivrosPage(driver, BASE_URL).open();

        livrosPage.criarLivro(titulo, autor, qtd, preco);
        String toast = livrosPage.getToastText();

        assertTrue(toast.contains("Livro criado"),
                "Toast deveria indicar criação de livro com sucesso");

        // título é salvo com trim no service, então comparo com trim também
        assertTrue(livrosPage.existeLivroComTitulo(titulo.trim()),
                "Livro deveria aparecer na tabela após criação");
    }

    @ParameterizedTest
    @DisplayName("Validar erros ao tentar criar cliente com dados inválidos")
    @CsvSource({
            // nome        , email               , fragmentoErro
            "'',             valido@email.com   , nome",
            "'   ',          valido@email.com   , nome",
            "Fulano,         ''                  , email",
            "Fulano,         '   '               , email"
    })
    void deveMostrarMensagensDeErroAoCriarClienteInvalido(String nome, String email, String fragmentoErro) {
        // RUBRICA 2 + 4: parametrizado + erro de validação
        var clientesPage = new ClientesPage(driver, BASE_URL).open();

        clientesPage.criarCliente(nome, email);
        String toast = clientesPage.getToastText();

        // A mensagem real vem como "HTTP 400 - Campo 'nome' é obrigatório"
        // ou "HTTP 400 - Campo 'email' é obrigatório"
        String lower = toast.toLowerCase();
        assertTrue(lower.contains("http 400"),
                "Deveria retornar erro HTTP 400");
        assertTrue(lower.contains(fragmentoErro.toLowerCase()),
                "Mensagem deveria mencionar o campo problemático (" + fragmentoErro + ")");
    }
}
