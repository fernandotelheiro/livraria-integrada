package com.livraria.support.services;

import com.livraria.support.models.Ticket;
import com.livraria.support.models.TicketPrioridade;
import com.livraria.support.models.TicketStatus;
import com.livraria.support.repositories.TicketRepository;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {

    // ---------------------------------------- Repositórios falsos (para testes) ----------------

    /** Repositório em memória – simula funcionamento normal do CSV. */
    static class InMemoryTicketRepository implements TicketRepository {
        private final Map<Integer, Ticket> db = new HashMap<>();
        private int seq = 0;

        @Override
        public List<Ticket> listar() {
            return new ArrayList<>(db.values());
        }

        @Override
        public Optional<Ticket> buscarPorId(int id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public Ticket criar(Ticket ticket) {
            int id = ++seq;
            ticket.setId(id);
            db.put(id, ticket);
            return ticket;
        }

        @Override
        public Ticket atualizar(Ticket ticket) {
            db.put(ticket.getId(), ticket);
            return ticket;
        }

        @Override
        public void excluir(int id) {
            db.remove(id);
        }
    }

    /** Repositório que SEMPRE falha – simula timeout/rede/IO quebrando. */
    static class FailingTicketRepository implements TicketRepository {
        private final RuntimeException toThrow;

        FailingTicketRepository(RuntimeException toThrow) {
            this.toThrow = toThrow;
        }

        private <T> T fail() {
            throw toThrow;
        }

        @Override
        public List<Ticket> listar() {
            return fail();
        }

        @Override
        public Optional<Ticket> buscarPorId(int id) {
            return fail();
        }

        @Override
        public Ticket criar(Ticket ticket) {
            return fail();
        }

        @Override
        public Ticket atualizar(Ticket ticket) {
            return fail();
        }

        @Override
        public void excluir(int id) {
            fail();
        }
    }

    // ----------------------------------------------- Testes principais --------------------

    @Test
    @DisplayName("criar() deve registrar ticket com dados válidos e defaults para campos opcionais")
    void criarDeveFuncionarComDadosValidos() {
        TicketService service = new TicketService(new InMemoryTicketRepository());

        Ticket t = service.criar(
                "Ajuda",
                "algum problema",
                "cliente@exemplo.com",
                "CRITICA",
                "" // status vazio -> default ABERTO
        );

        assertTrue(t.getId() > 0);
        assertEquals("Ajuda", t.getTitulo());
        assertEquals("algum problema", t.getDescricao());
        assertEquals("cliente@exemplo.com", t.getEmailCliente());
        assertEquals(TicketPrioridade.CRITICA, t.getPrioridade());
        assertEquals(TicketStatus.ABERTO, t.getStatus());
    }

    @Test
    @DisplayName("atualizar() deve alterar um ticket existente mantendo o mesmo id")
    void atualizarDeveAlterarTicket() {
        TicketService service = new TicketService(new InMemoryTicketRepository());
        Ticket criado = service.criar("A", "desc", "c@e.com", "ALTA", "ABERTO");

        Ticket atualizado = service.atualizar(
                criado.getId(),
                "Novo titulo",
                "Nova desc",
                "novo@e.com",
                "MEDIA",
                "EM_ANDAMENTO"
        );

        assertEquals(criado.getId(), atualizado.getId());
        assertEquals("Novo titulo", atualizado.getTitulo());
        assertEquals("Nova desc", atualizado.getDescricao());
        assertEquals("novo@e.com", atualizado.getEmailCliente());
        assertEquals(TicketPrioridade.MEDIA, atualizado.getPrioridade());
        assertEquals(TicketStatus.EM_ANDAMENTO, atualizado.getStatus());
    }

    @Test
    @DisplayName("buscarPorId() deve lançar 404 quando id não existe (fail gracefully)")
    void buscarPorIdDeveLancarNotFound() {
        TicketService service = new TicketService(new InMemoryTicketRepository());
        assertThrows(NotFoundResponse.class, () -> service.buscarPorId(999));
    }

    @Test
    @DisplayName("criar() deve falhar cedo (fail early) se título estiver vazio")
    void criarDeveFalharSemTitulo() {
        TicketService service = new TicketService(new InMemoryTicketRepository());

        BadRequestResponse ex = assertThrows(
                BadRequestResponse.class,
                () -> service.criar("   ", "desc", "c@e.com", "BAIXA", "ABERTO")
        );

        assertEquals("Título é obrigatório.", ex.getMessage());
    }

    @Test
    @DisplayName("criar() deve falhar cedo para prioridade inválida")
    void criarDeveFalharComPrioridadeInvalida() {
        TicketService service = new TicketService(new InMemoryTicketRepository());

        BadRequestResponse ex = assertThrows(
                BadRequestResponse.class,
                () -> service.criar("Titulo", "desc", "c@e.com", "MUITO_ALTA", "ABERTO")
        );

        assertTrue(ex.getMessage().contains("Prioridade inválida"));
    }

    @Test
    @DisplayName("atualizar() deve falhar cedo para status inválido")
    void atualizarDeveFalharComStatusInvalido() {
        TicketService service = new TicketService(new InMemoryTicketRepository());
        Ticket criado = service.criar("T", "d", "c@e.com", "ALTA", "ABERTO");

        BadRequestResponse ex = assertThrows(
                BadRequestResponse.class,
                () -> service.atualizar(criado.getId(), "T2", "d2", "c2@e.com", "ALTA", "FECHADOOO")
        );

        assertTrue(ex.getMessage().contains("Status inválido"));
    }

    @Test
    @DisplayName("Service deve reagir a RuntimeException do repositório (simulação de timeout/rede)")
    void deveReagirQuandoRepositorioQuebra() {
        RuntimeException repoError = new RuntimeException("Simulação de timeout");
        TicketService service = new TicketService(new FailingTicketRepository(repoError));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.criar("T", "d", "c@e.com", "ALTA", "ABERTO")
        );

        assertEquals("Simulação de timeout", ex.getMessage());
        // A tradução disso para HTTP 500 + mensagem segura é feita em MainSupport
        // (exception handler para Exception.class) -> fail gracefully.
    }

    @Test
    @DisplayName("Service deve suportar criação de muitos tickets sem falhar (sobrecarga leve)")
    void deveSuportarCriacaoDeMuitosTicketsSemFalhar() {
        InMemoryTicketRepository repo = new InMemoryTicketRepository();
        TicketService service = new TicketService(repo);

        int quantidade = 300; // "sobrecarga" leve para o contexto do TP3

        for (int i = 0; i < quantidade; i++) {
            service.criar(
                    "Titulo " + i,
                    "Descricao " + i,
                    "user" + i + "@teste.com",
                    "MEDIA",
                    "ABERTO"
            );
        }

        List<Ticket> todos = repo.listar();

        assertTrue(
                todos.size() >= quantidade,
                "Quantidade de tickets persistidos (" + todos.size() +
                        ") é menor que a quantidade criada (" + quantidade + ")"
        );
    }

    // ---------------------------------- Testes parametrizados --------------------

    @ParameterizedTest
    @CsvSource({
            "BAIXA, BAIXA",
            "baixa, BAIXA",
            "'  media  ', MEDIA",
            "ALTA, ALTA",
            "critica, CRITICA"
    })
    @DisplayName("criar() normaliza prioridade e atribui o enum correto (teste parametrizado)")
    void criarNormalizaPrioridade(String prioridadeRaw, String prioridadeEsperada) {
        TicketService service = new TicketService(new InMemoryTicketRepository());

        Ticket t = service.criar(
                "Titulo param",
                "Desc param",
                "cliente@param.com",
                prioridadeRaw,
                "ABERTO"
        );

        assertEquals(
                TicketPrioridade.valueOf(prioridadeEsperada),
                t.getPrioridade()
        );
    }

    @ParameterizedTest
    @CsvSource({
            "ABERTO, ABERTO",
            "aberto, ABERTO",
            "' EM_ANDAMENTO ', EM_ANDAMENTO",
            "resolvido, RESOLVIDO",
            "cancelado, CANCELADO",
            "'', ABERTO"
    })
    @DisplayName("criar() normaliza status e usa ABERTO como default quando string vazia (teste parametrizado)")
    void criarNormalizaStatus(String statusRaw, String statusEsperado) {
        TicketService service = new TicketService(new InMemoryTicketRepository());

        Ticket t = service.criar(
                "Titulo status",
                "Desc status",
                "cliente-status@teste.com",
                "MEDIA",
                statusRaw
        );

        assertEquals(
                TicketStatus.valueOf(statusEsperado),
                t.getStatus()
        );
    }

    // -------------------- Fuzz tests --------------------

    @Test
    @DisplayName("Fuzz testing simples: entradas randômicas nunca podem causar erros inesperados")
    void fuzzTestingEmCamposDeTexto() {
        TicketService service = new TicketService(new InMemoryTicketRepository());
        Random random = new Random(123);

        for (int i = 0; i < 200; i++) {
            String titulo = random.nextBoolean() ? "Titulo " + i : "   "; // às vezes inválido
            String descricao = "desc randômica " + random.nextInt();
            String email = "user" + i + "@teste.com"; // mantem e-mail bem formado aqui
            String prioridade = switch (random.nextInt(6)) {
                case 0 -> "BAIXA";
                case 1 -> "MEDIA";
                case 2 -> "ALTA";
                case 3 -> "CRITICA";
                case 4 -> "";             // default
                default -> "QUALQUER_COISA"; // inválida
            };
            String status = switch (random.nextInt(6)) {
                case 0 -> "ABERTO";
                case 1 -> "EM_ANDAMENTO";
                case 2 -> "RESOLVIDO";
                case 3 -> "CANCELADO";
                case 4 -> "";           // default
                default -> "ESTRANHO";  // inválido
            };

            try {
                service.criar(titulo, descricao, email, prioridade, status);
            } catch (BadRequestResponse expected) {
                // Esperado para entradas inválidas:
                // 1) não travar, 2) não lançar exceções inesperadas (NullPointer, etc).
            }
        }

        // Se chegar aqui sem explodir com exceção fora de BadRequestResponse,entao o fuzz básico passou
        assertTrue(true);
    }

    @Test
    @DisplayName("Fuzz leve: prioridades aleatórias inválidas não derrubam o serviço")
    void fuzzPrioridadesInvalidas() {
        TicketService service = new TicketService(new InMemoryTicketRepository());

        for (int i = 0; i < 30; i++) {
            String random = UUID.randomUUID().toString(); // lixo aleatório
            assertThrows(BadRequestResponse.class, () -> service.criar(
                    "Titulo",
                    "Desc",
                    "cli@x.com",
                    random,
                    "ABERTO"
            ));
        }
    }

    @Test
    @DisplayName("Fuzz leve: prioridades aleatórias inválidas não derrubam o serviço (com dados variando)")
    void fuzzPrioridadesInvalidas_naoDerrubamServico() {
        TicketService service = new TicketService(new InMemoryTicketRepository());

        for (int i = 0; i < 50; i++) {
            // gera uma string aleatória totalmente fora do enum
            String randomPrioridade = UUID.randomUUID().toString();

            // variáveis efetivamente finais para a lambda
            final String titulo = "Titulo " + i;
            final String descricao = "Descricao " + i;
            final String email = "cliente" + i + "@teste.com";

            assertThrows(BadRequestResponse.class, () -> service.criar(
                    titulo,
                    descricao,
                    email,
                    randomPrioridade,   // prioridade inválida
                    "ABERTO"            // status válido
            ));
        }
    }

    @Test
    @DisplayName("Fuzz leve: status aleatórios inválidos não derrubam o serviço")
    void fuzzStatusInvalidos_naoDerrubamServico() {
        TicketService service = new TicketService(new InMemoryTicketRepository());

        for (int i = 0; i < 50; i++) {
            String randomStatus = UUID.randomUUID().toString();

            final String titulo = "Titulo " + i;
            final String descricao = "Descricao " + i;
            final String email = "cliente" + i + "@teste.com";

            assertThrows(BadRequestResponse.class, () -> service.criar(
                    titulo,
                    descricao,
                    email,
                    "ALTA",             // prioridade válida
                    randomStatus        // status inválido
            ));
        }
    }
}
