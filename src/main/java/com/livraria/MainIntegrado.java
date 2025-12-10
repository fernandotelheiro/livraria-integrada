package com.livraria;

import com.livraria.controllers.ClienteController;
import com.livraria.controllers.LivroController;
import com.livraria.controllers.RelatorioController;
import com.livraria.shared.utils.Paths;
import com.livraria.utils.SeedData;

import com.livraria.support.controllers.TicketController;
import com.livraria.support.api.ApiError;
import com.livraria.support.exceptions.NotFoundException;
import com.livraria.support.exceptions.ValidationException;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.NoSuchElementException;

public class MainIntegrado {

    private static final Logger log = LoggerFactory.getLogger(MainIntegrado.class);

    public static void main(String[] args) {

        // ==== INICIALIZA OS ARQUIVOS DA LIVRARIA ====
        SeedData.ensureFiles();
        System.out.println("DATA_DIR  = " + Paths.DATA_DIR.toAbsolutePath());
        System.out.println("LIVROS    = " + Paths.LIVROS.toAbsolutePath());
        System.out.println("CLIENTES  = " + Paths.CLIENTES.toAbsolutePath());
        System.out.println("LOG       = " + Paths.LOG.toAbsolutePath());

        // ==== INICIALIZA O CSV DE TICKETS DO SUPORTE ====
        com.livraria.support.utils.SeedData.ensureTicketsCsvExists();
        log.info("Tickets CSV ok. DATA_DIR = {}", Paths.DATA_DIR.toAbsolutePath());

        // ==== CRIA O APP JAVALIN (ARQUIVOS ESTÁTICOS) ====
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "public";      // src/main/resources/public
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress = false;
                staticFiles.headers.put("Cache-Control", "max-age=0");
            });
        });

        // ==== HANDLERS GLOBAIS DE ERRO ====

        // Erros de regras da livraria (já existiam no Main)
        app.exception(IllegalArgumentException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("error", e.getMessage())));
        app.exception(NoSuchElementException.class, (e, ctx) ->
                ctx.status(404).json(Map.of("error", e.getMessage())));

        // Erros de domínio do suporte
        app.exception(ValidationException.class, (e, ctx) -> {
            log.warn("Erro de validação: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(new ApiError("VALIDATION_ERROR", e.getMessage()));
        });

        app.exception(NotFoundException.class, (e, ctx) -> {
            log.warn("Recurso não encontrado: {}", e.getMessage());
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(new ApiError("NOT_FOUND", e.getMessage()));
        });

        // Qualquer outro erro inesperado
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Erro inesperado", e);
            if (!ctx.res().isCommitted()) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(new ApiError(
                                "UNEXPECTED_ERROR",
                                "Ocorreu um erro interno. Tente novamente mais tarde."
                        ));
            }
        });

        // 404 padrão de API
        app.error(404, ctx -> {
            if (!ctx.res().isCommitted()) {
                ctx.json(new ApiError("NOT_FOUND", "Rota não encontrada"));
            }
        });

        // Força no-cache pra qualquer coisa em /api/*
        app.after("/api/*", ctx -> {
            ctx.header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            ctx.header("Pragma", "no-cache");
            ctx.header("Expires", "0");
        });

        // ==== CONTROLLERS ====
        var livroController = new LivroController();
        var clienteController = new ClienteController();
        var relatorioController = new RelatorioController();
        var ticketController = new TicketController();

        // ==== ROTAS DA LIVRARIA ====
        app.get("/api/livros", livroController::listar);
        app.post("/api/livros", livroController::criar);
        app.put("/api/livros/{id}", livroController::atualizar);
        app.delete("/api/livros/{id}", livroController::excluir);

        app.get("/api/clientes", clienteController::listar);
        app.post("/api/clientes", clienteController::criar);
        app.put("/api/clientes/{id}", clienteController::atualizar);
        app.delete("/api/clientes/{id}", clienteController::excluir);

        app.post("/api/compras", livroController::comprar);
        app.get("/api/relatorio", relatorioController::listar);

        // ==== ROTAS DO SUPORTE ====
        app.get("/api/tickets", ticketController::listar);
        app.get("/api/tickets/{id}", ticketController::obter);
        app.post("/api/tickets", ticketController::criar);
        app.put("/api/tickets/{id}", ticketController::atualizar);
        app.delete("/api/tickets/{id}", ticketController::excluir);

        // ==== FRONT: redireciona "/" para o index da livraria ====
        app.get("/", ctx -> ctx.redirect("/index.html"));

        // ==== START (UMA PORTA SÓ PARA TUDO) ====
        app.start(7000);
        log.info("Servidor integrado (TP4) rodando em http://localhost:7000/");
    }
}
