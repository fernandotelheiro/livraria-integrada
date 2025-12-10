package com.livraria.support.ui;

import com.livraria.support.MainSupport;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TicketUiTest {

    private static final String BASE_URL = "http://localhost:7000/";
    private static Thread serverThread;

    private WebDriver driver;

    // ------------------------------------------ Infra do servidor -------------------

    @BeforeAll
    static void setUpAll() throws Exception {
        // Sobe o servidor TP3 em uma thread separada
        serverThread = new Thread(() -> MainSupport.main(new String[0]));
        serverThread.setDaemon(true);
        serverThread.start();

        // Esperar a porta 7000 ficar disponível
        aguardarServidorSubir("localhost", 7000, Duration.ofSeconds(20));
    }

    private static void aguardarServidorSubir(String host, int port, Duration timeout) throws InterruptedException {
        long inicio = System.currentTimeMillis();
        long limite = inicio + timeout.toMillis();

        while (System.currentTimeMillis() < limite) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 500);
                return; // conectou, servidor está de pé
            } catch (Exception e) {
                Thread.sleep(500);
            }
        }
        throw new IllegalStateException("Servidor não subiu em " + timeout.toSeconds() + " segundos");
    }

    @AfterAll
    static void tearDownAll() {
        // Servidor está em thread daemon, encerra junto com a JVM.
    }

    // -------------------------------- Setup/teardown do WebDriver -------------------

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.get(BASE_URL + "support-index.html");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------------------------------------- Helpers de página -------------------

    private int getRowCount() {
        List<WebElement> linhas = driver.findElements(By.cssSelector("#tbl-tickets tbody tr"));
        return linhas.size();
    }

    private void preencherCamposFormulario(String titulo,
                                           String descricao,
                                           String email,
                                           String prioridade,
                                           String status) {
        WebElement tituloEl = driver.findElement(By.id("titulo"));
        tituloEl.clear();
        tituloEl.sendKeys(titulo);

        WebElement descEl = driver.findElement(By.id("descricao"));
        descEl.clear();
        descEl.sendKeys(descricao);

        WebElement emailEl = driver.findElement(By.id("emailCliente"));
        emailEl.clear();
        emailEl.sendKeys(email);

        new Select(driver.findElement(By.id("prioridade"))).selectByValue(prioridade);
        new Select(driver.findElement(By.id("status"))).selectByValue(status);
    }

    private void aguardarMudancaLinhas(int linhasAntes) throws InterruptedException {
        long limite = System.currentTimeMillis() + 5000; // 5s
        while (System.currentTimeMillis() < limite) {
            int atual = getRowCount();
            if (atual != linhasAntes) {
                return;
            }
            Thread.sleep(200);
        }
        // se não mudar, deixa o teste falhar lá na asserção
    }

    // ------------------------------------- Tests -------------------

    @Test
    @Order(1)
    @DisplayName("Deve criar ticket via formulário (fluxo feliz)")
    void deveCriarTicketViaFormulario() throws InterruptedException {
        int linhasAntes = getRowCount();

        preencherCamposFormulario(
                "Bug no sistema",
                "Erro ao abrir tela X",
                "cliente@example.com",
                "ALTA",
                "ABERTO"
        );

        driver.findElement(By.id("btn-salvar")).click();

        aguardarMudancaLinhas(linhasAntes);
        int linhasDepois = getRowCount();

        assertTrue(linhasDepois > linhasAntes,
                "Esperava que o número de linhas da tabela aumentasse após criar o ticket.");

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Bug no sistema"),
                "Tabela deveria conter o título do ticket criado.");
    }

    @Test
    @Order(2)
    @DisplayName("Deve editar ticket via formulário")
    void deveEditarTicketViaFormulario() throws InterruptedException {
        // Garante que exista pelo menos um ticket (caso o teste anterior não tenha sido suficiente)
        int linhasAntes = getRowCount();
        if (linhasAntes == 0) {
            preencherCamposFormulario(
                    "Ticket inicial",
                    "Descrição inicial",
                    "cliente2@example.com",
                    "MEDIA",
                    "ABERTO"
            );
            driver.findElement(By.id("btn-salvar")).click();
            aguardarMudancaLinhas(linhasAntes);
        }

        // Clica no primeiro botão "Editar" da tabela
        WebElement btnEditar = driver.findElement(
                By.xpath("//table[@id='tbl-tickets']//button[contains(text(),'Editar')]")
        );
        btnEditar.click();

        // Altera os campos
        preencherCamposFormulario(
                "Ticket editado com sucesso",
                "Descrição alterada",
                "cliente2@example.com",
                "ALTA",
                "RESOLVIDO"
        );

        driver.findElement(By.id("btn-salvar")).click();

        // Espera algum processamento e recarregamento da tabela
        Thread.sleep(2000);

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Ticket editado com sucesso"),
                "Tabela deveria conter o novo título do ticket editado.");
    }

    @Test
    @Order(3)
    @DisplayName("Não deve criar ticket quando título estiver vazio")
    void deveMostrarErroQuandoTituloVazio() throws InterruptedException {
        int linhasAntes = getRowCount();

        // Deixa o título vazio de propósito
        WebElement titulo = driver.findElement(By.id("titulo"));
        titulo.clear();

        preencherCamposFormulario(
                "", // título vazio
                "Desc qualquer",
                "cliente3@example.com",
                "BAIXA",
                "ABERTO"
        );

        driver.findElement(By.id("btn-salvar")).click();

        // Dá um tempo para o browser aplicar validação nativa ou JS
        Thread.sleep(1500);

        int linhasDepois = getRowCount();
        assertEquals(linhasAntes, linhasDepois,
                "Não deveria ter sido criado um novo ticket com título vazio.");

        String pageSource = driver.getPageSource();
        assertFalse(pageSource.contains("Desc qualquer"),
                "A descrição não deveria aparecer na tabela se o ticket não foi criado.");
    }
}
