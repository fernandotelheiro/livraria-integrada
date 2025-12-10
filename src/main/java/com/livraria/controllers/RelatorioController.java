package com.livraria.controllers;

import com.livraria.shared.utils.Paths;
import io.javalin.http.Context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class RelatorioController {

    // ----------------- helpers null-safe -----------------
    private static String s(String v) { return v == null ? "" : v; }

    private static int parseInt(String v, int def) {
        try { return Integer.parseInt(s(v).trim()); } catch (Exception e) { return def; }
    }

    private static LocalDate parseDate(String v) {
        try {
            var x = s(v).trim();
            return x.isEmpty() ? null : LocalDate.parse(x);
        } catch (Exception e) { return null; }
    }

    private static String fmtTs(LocalDateTime ts) {
        return ts == null ? "" : ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ----------------- endpoint ----------
    public void listar(Context ctx) {
        // parâmetros (todos opcionais)
        int page = Math.max(1, parseInt(ctx.queryParam("page"), 1));
        int size = Math.max(1, Math.min(200, parseInt(ctx.queryParam("size"), 50)));
        String tipo    = s(ctx.queryParam("tipo")).trim().toUpperCase();
        String cliente = s(ctx.queryParam("cliente")).trim().toLowerCase();
        String livro   = s(ctx.queryParam("livro")).trim().toLowerCase();
        LocalDate de   = parseDate(ctx.queryParam("de"));
        LocalDate ate  = parseDate(ctx.queryParam("ate"));

        var eventos = new ArrayList<Map<String, Object>>();

        try (var lines = Files.lines(Paths.LOG, StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                if (line == null || line.isBlank()) return;

                // split "timestamp - message"
                int sep = line.indexOf(" - ");
                if (sep < 0) return;
                String tsStr = line.substring(0, sep).trim();
                String msg   = line.substring(sep + 3).trim();

                // timestamp
                LocalDateTime ts;
                try { ts = LocalDateTime.parse(tsStr); } catch (Exception e) { ts = null; }

                // mensagem -> tipo + pares chave=valor
                String[] parts = msg.split("\\|");
                if (parts.length == 0) return;
                String t = parts[0].trim(); // COMPRA, CRIACAO, ATUALIZACAO, EXCLUSAO, BLOQUEADA...
                var kv = new HashMap<String, String>();
                for (int i = 1; i < parts.length; i++) {
                    String p = parts[i];
                    int eq = p.indexOf('=');
                    if (eq > 0) {
                        String k = p.substring(0, eq).trim().toLowerCase();
                        String v = p.substring(eq + 1).trim();
                        kv.put(k, v);
                    }
                }

                // filtros
                if (de  != null && ts != null && ts.toLocalDate().isBefore(de))   return;
                if (ate != null && ts != null && ts.toLocalDate().isAfter(ate))    return;
                if (!tipo.isEmpty() && !t.equalsIgnoreCase(tipo))                  return;
                if (!cliente.isEmpty() && !s(kv.get("cliente")).toLowerCase().contains(cliente)) return;
                if (!livro.isEmpty()   && !s(kv.get("livro")).toLowerCase().contains(livro))     return;

                // monta linha
                var row = new LinkedHashMap<String, Object>();
                row.put("timestamp", fmtTs(ts));
                row.put("tipo", t);
                row.put("cliente", s(kv.get("cliente")));
                row.put("livro", s(kv.get("livro")));
                row.put("quantidade", parseInt(kv.get("qtd"), 0));

                if (kv.containsKey("antes") && kv.containsKey("depois")) {
                    row.put("estoqueAntes", parseInt(kv.get("antes"), 0));
                    row.put("estoqueDepois", parseInt(kv.get("depois"), 0));
                } else {
                    row.put("estoqueAntes", null);
                    row.put("estoqueDepois", null);
                }
                row.put("mensagem", msg);

                eventos.add(row);
            });
        } catch (IOException e) {
            ctx.status(500).json(Map.of("error", "Falha ao ler log: " + e.getMessage()));
            return;
        }

        // totais (soma de itens vendidos em eventos COMPRA)
        long itensVendidos = eventos.stream()
                .filter(m -> "COMPRA".equals(m.get("tipo")))
                .mapToLong(m -> ((Number) m.getOrDefault("quantidade", 0)).longValue())
                .sum();

        // paginacaoo
        int from = Math.max(0, (page - 1) * size);
        int to   = Math.min(eventos.size(), from + size);
        var pageData = from >= to ? List.<Map<String, Object>>of() : eventos.subList(from, to);

        var resp = new LinkedHashMap<String, Object>();
        resp.put("dados", pageData);
        resp.put("totais", Map.of("linhas", eventos.size(), "itensVendidos", itensVendidos));
        resp.put("paginacao", Map.of("page", page, "size", size, "hasNext", to < eventos.size()));

        ctx.json(resp);
    }
}
