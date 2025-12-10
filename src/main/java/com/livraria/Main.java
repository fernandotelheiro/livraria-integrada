package com.livraria;

import com.livraria.controllers.ClienteController;
import com.livraria.controllers.LivroController;
import com.livraria.controllers.RelatorioController;
import com.livraria.shared.utils.Paths;
import com.livraria.utils.SeedData;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;
import java.util.NoSuchElementException;

public class Main {
    public static void main(String[] args) {

        // 1. Garante arquivos persistentes fora do classpath
        SeedData.ensureFiles();
        System.out.println("DATA_DIR  = " + Paths.DATA_DIR.toAbsolutePath());
        System.out.println("LIVROS    = " + Paths.LIVROS.toAbsolutePath());
        System.out.println("CLIENTES  = " + Paths.CLIENTES.toAbsolutePath());
        System.out.println("LOG       = " + Paths.LOG.toAbsolutePath());

        // 2. Cria o app (serve /public do classpath)
        Javalin app = Javalin.create(cfg -> {
            cfg.staticFiles.add("/public", Location.CLASSPATH);
        });

        // 3. Handlers globais de erro (evita 500 genérico)
        app.exception(IllegalArgumentException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("error", e.getMessage())));
        app.exception(NoSuchElementException.class, (e, ctx) ->
                ctx.status(404).json(Map.of("error", e.getMessage())));
        app.exception(RuntimeException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("error", e.getMessage())));

        //3.1 Força no-cache para todas as respostas de API
        app.after("/api/*", ctx -> {
            ctx.header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            ctx.header("Pragma", "no-cache");
            ctx.header("Expires", "0");
        });

        // 4. Controllers
        var livroController = new LivroController();
        var clienteController = new ClienteController();
        var relatorioController = new RelatorioController();

        // 5. Rotas - LIVROS
        app.get("/api/livros", livroController::listar);
        app.post("/api/livros", livroController::criar);
        app.put("/api/livros/{id}", livroController::atualizar);
        app.delete("/api/livros/{id}", livroController::excluir);

        // 6. Rotas - CLIENTES
        app.get("/api/clientes", clienteController::listar);
        app.post("/api/clientes", clienteController::criar);
        app.put("/api/clientes/{id}", clienteController::atualizar);
        app.delete("/api/clientes/{id}", clienteController::excluir);

        // 7. Rotas - COMPRAS
        app.post("/api/compras", livroController::comprar);

        // 8. Rotas - RELATÓRIO
        app.get("/api/relatorio", relatorioController::listar);

        // 9. Front
        app.get("/", ctx -> ctx.redirect("/index.html"));

        // 10. Start
        app.start(7000);
        app.get("/api/_debug/paths", ctx -> ctx.json(Map.of(
                "dataDir", Paths.DATA_DIR.toString(),
                "livros",  Paths.LIVROS.toString(),
                "clientes", Paths.CLIENTES.toString(),
                "log",     Paths.LOG.toString()
        )));

    }
}
