package com.livraria.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.models.Livro;
import com.livraria.services.LivroService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

public class LivroController {

    private final LivroService service = new LivroService();
    private final ObjectMapper om = new ObjectMapper();

    // DTO (Data Transfer Object)
    public static class LivroDTO {
        public String titulo;
        public String autor;
        public String preco;      // pode vir "49,90" ou "49.90" (ou número no JSON)
        public Integer quantidade;
    }
    public static class CompraDTO {
        public String titulo;
        public String cliente;
        public Integer quantidade;
    }

    public void listar(Context ctx) {
        List<Livro> livros = service.listar();
        ctx.json(livros);
    }

    public void criar(Context ctx) {
        try {
            LivroDTO dto = readLivroDTO(ctx);
            var criado = service.criar(new Livro(
                    0,
                    nvl(dto.titulo).trim(),
                    nvl(dto.autor).trim(),
                    dto.quantidade == null ? 0 : dto.quantidade,
                    parseDoubleFlex(dto.preco)
            ));
            ctx.status(201).json(criado);
        } catch (RuntimeException re) {
            ctx.status(400).json(Map.of("error", re.getMessage()));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Entrada inválida: " + e.getMessage()));
        }
    }

    public void atualizar(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            LivroDTO dto = readLivroDTO(ctx);
            var atualizado = service.atualizar(id, new Livro(
                    id,
                    nvl(dto.titulo).trim(),
                    nvl(dto.autor).trim(),
                    dto.quantidade == null ? 0 : dto.quantidade,
                    parseDoubleFlex(dto.preco)
            ));
            ctx.json(atualizado);
        } catch (RuntimeException re) {
            ctx.status(404).json(Map.of("error", re.getMessage()));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Entrada inválida: " + e.getMessage()));
        }
    }

    public void excluir(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            service.excluir(id);
            ctx.status(204);
        } catch (RuntimeException re) {
            ctx.status(404).json(Map.of("error", re.getMessage()));
        }
    }

    public void comprar(Context ctx) {
        try {
            CompraDTO dto = readCompraDTO(ctx);
            service.comprar(
                    nvl(dto.titulo).trim(),
                    nvl(dto.cliente).trim(),
                    dto.quantidade == null ? 1 : dto.quantidade
            );
            ctx.status(201).json(Map.of("ok", true));
        } catch (RuntimeException re) {
            ctx.status(400).json(Map.of("error", re.getMessage()));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Entrada inválida: " + e.getMessage()));
        }
    }

    // ---------- helpers ----------
    private static String nvl(String s) { return s == null ? "" : s; }
    private static double parseDoubleFlex(String raw) {
        if (raw == null) return 0.0;
        return Double.parseDouble(raw.trim().replace(",", "."));
    }

    private LivroDTO readLivroDTO(Context ctx) throws Exception {
        String ct = nvl(ctx.contentType());
        if (ct.contains("application/json")) {
            return om.readValue(ctx.body(), LivroDTO.class);
        }
        // fallback para form urlencoded (se o servidor não receber JSON, ele tenta ler os dados no formato padrão de formulários do HTML)
        LivroDTO dto = new LivroDTO();
        dto.titulo = ctx.formParam("titulo");
        dto.autor = ctx.formParam("autor");
        dto.preco = ctx.formParam("preco");
        String q = ctx.formParam("quantidade");
        dto.quantidade = (q == null || q.isBlank()) ? 0 : Integer.parseInt(q);
        return dto;
    }

    private CompraDTO readCompraDTO(Context ctx) throws Exception {
        String ct = nvl(ctx.contentType());
        if (ct.contains("application/json")) {
            return om.readValue(ctx.body(), CompraDTO.class);
        }
        CompraDTO dto = new CompraDTO();
        dto.titulo = ctx.formParam("titulo");
        dto.cliente = ctx.formParam("cliente");
        String q = ctx.formParam("quantidade");
        dto.quantidade = (q == null || q.isBlank()) ? 1 : Integer.parseInt(q);
        return dto;
    }
}
