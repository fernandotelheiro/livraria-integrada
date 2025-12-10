package com.livraria.support;

import com.livraria.support.api.ApiError;
import com.livraria.support.controllers.TicketController;
import com.livraria.support.exceptions.NotFoundException;
import com.livraria.support.exceptions.ValidationException;
import com.livraria.shared.utils.Paths;
import com.livraria.support.utils.SeedData;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MainSupport {

    private static final Logger log = LoggerFactory.getLogger(MainSupport.class);

    public static void main(String[] args) {

        // ====== inicializa diretório / CSV de dados (fail gracefully p/ entrada/saida) ======
        Path dataDir = Paths.DATA_DIR; // <-- antes: Paths.dataDir()
        log.info("Usando diretório de dados: {}", dataDir.toAbsolutePath());
        SeedData.ensureTicketsCsvExists();

        // ====== cria app Javalin e configura arquivos estáticos ======
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                // aqui é a pasta de frontend dentro do classpath (src/main/resources/public)
                staticFiles.directory = "public";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress = false;
                staticFiles.headers.put("Cache-Control", "max-age=0");
            });
        });

        // ================================================== HANDLERS GLOBAIS DE ERRO (fail gracefully) ============

        // Erros de validação de domínio
        app.exception(ValidationException.class, (e, ctx) -> {
            log.warn("Erro de validação: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(new ApiError("VALIDATION_ERROR", e.getMessage()));
        });

        // Erros de "não encontrado" de domínio
        app.exception(NotFoundException.class, (e, ctx) -> {
            log.warn("Recurso não encontrado: {}", e.getMessage());
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(new ApiError("NOT_FOUND", e.getMessage()));
        });

        // Qualquer outro erro inesperado (IO, NullPointer, etc.)
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

        // 404 de rota não encontrada
        app.error(404, ctx -> {
            if (!ctx.res().isCommitted()) {
                ctx.json(new ApiError("NOT_FOUND", "Rota não encontrada"));
            }
        });

        // ===================================================== ROTAS ============

        TicketController ticketController = new TicketController();

        app.get("/api/tickets", ticketController::listar);
        app.get("/api/tickets/{id}", ticketController::obter);
        app.post("/api/tickets", ticketController::criar);
        app.put("/api/tickets/{id}", ticketController::atualizar);
        app.delete("/api/tickets/{id}", ticketController::excluir);
// rota para a página HTML do sistema de suporte
        app.get("/", ctx -> ctx.redirect("/support-index.html"));
        app.start(7100);
        log.info("Servidor TP3 rodando em http://localhost:7100/");
    }
}
