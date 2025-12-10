package com.livraria.controllers;

import com.livraria.models.Cliente;
import com.livraria.services.ClienteService;
import io.javalin.http.Context;

public class ClienteController {

    private final ClienteService service = new ClienteService();

    public void listar(Context ctx) {
        ctx.json(service.listar());
    }

    public void criar(Context ctx) {
        var req = ctx.bodyAsClass(NovoCliente.class);
        String nome  = safe(req.nome);
        String email = safe(req.email);

        if (nome.isEmpty())  throw new IllegalArgumentException("Campo 'nome' é obrigatório");
        if (email.isEmpty()) throw new IllegalArgumentException("Campo 'email' é obrigatório");

        var criado = service.criar(new Cliente(0, nome, email));
        ctx.status(201).json(criado);
    }

    public void atualizar(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = ctx.bodyAsClass(NovoCliente.class);
        String nome  = safe(req.nome);
        String email = safe(req.email);

        if (nome.isEmpty())  throw new IllegalArgumentException("Campo 'nome' é obrigatório");
        if (email.isEmpty()) throw new IllegalArgumentException("Campo 'email' é obrigatório");

        var atualizado = service.atualizar(id, new Cliente(id, nome, email));
        ctx.json(atualizado);
    }

    public void excluir(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        service.excluir(id);
        ctx.status(204);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    // DTO esperado no JSON do front: (nome e email)
    public static class NovoCliente { public String nome; public String email; }
}
