package com.livraria.services;

import com.livraria.models.LogEntry;
import com.livraria.models.LogEntry.Tipo;
import com.livraria.shared.utils.Paths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogService {

    // Regex para a linha de COMPRA
    private static final Pattern COMPRA = Pattern.compile(
            "^AÇÃO:\\s*Compra\\s*\\|\\s*Livro:\\s*'(?<livro>[^']+)'\\s*\\|\\s*Cliente:\\s*(?<cliente>[^|]+?)\\s*\\|\\s*Qtd:\\s*(?<qtd>\\d+)\\s*\\|\\s*Estoque:\\s*(?<antes>\\d+)\\s*->\\s*(?<depois>\\d+)\\s*$"
    );

    private static final Pattern CRIACAO = Pattern.compile(
            "^CRIAÇÃO:\\s*Livro\\s*'(?<livro>[^']+)'\\s*\\(id=\\d+\\),\\s*qtd=(?<qtd>\\d+)\\)$"
    );

    private static final Pattern ATUALIZACAO = Pattern.compile(
            "^ATUALIZAÇÃO:\\s*Livro\\s*id=\\d+\\s*agora\\s*qtd=(?<qtd>\\d+)\\s*$"
    );

    private static final Pattern EXCLUSAO = Pattern.compile(
            "^EXCLUSÃO:\\s*Livro\\s*id=\\d+\\s*removido.*$"
    );

    private static final Pattern BLOQUEADA = Pattern.compile(
            "^TENTATIVA\\s+EXCLUSÃO\\s+BLOQUEADA:.*$"
    );

    public List<LogEntry> lerTudo() {
        var entries = new ArrayList<LogEntry>();
        if (!Files.exists(Paths.LOG)) return entries;

        try (var lines = Files.lines(Paths.LOG, StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                if (line.isBlank()) return;
                int pos = line.indexOf(" - ");
                if (pos <= 0) return;

                String ts = line.substring(0, pos).trim();
                String msg = line.substring(pos + 3).trim();

                LocalDateTime timestamp;
                try {
                    // ISO_LOCAL_DATE_TIME (aceita fração de segundos)
                    timestamp = LocalDateTime.parse(ts);
                } catch (Exception e) {
                    return;
                }

                // tenta dar match em ordem de maior valor informativo
                Matcher m = COMPRA.matcher(msg);
                if (m.find()) {
                    String livro = m.group("livro").trim();
                    String cliente = m.group("cliente").trim();
                    Integer qtd = Integer.parseInt(m.group("qtd"));
                    Integer antes = Integer.parseInt(m.group("antes"));
                    Integer depois = Integer.parseInt(m.group("depois"));
                    entries.add(new LogEntry(timestamp, Tipo.COMPRA, cliente, livro, qtd, antes, depois, msg));
                    return;
                }

                m = CRIACAO.matcher(msg);
                if (m.find()) {
                    String livro = m.group("livro").trim();
                    Integer qtd = Integer.parseInt(m.group("qtd"));
                    entries.add(new LogEntry(timestamp, Tipo.CRIACAO, null, livro, qtd, null, null, msg));
                    return;
                }

                m = ATUALIZACAO.matcher(msg);
                if (m.find()) {
                    Integer qtd = Integer.parseInt(m.group("qtd"));
                    entries.add(new LogEntry(timestamp, Tipo.ATUALIZACAO, null, null, qtd, null, null, msg));
                    return;
                }

                if (EXCLUSAO.matcher(msg).find()) {
                    entries.add(new LogEntry(timestamp, Tipo.EXCLUSAO, null, null, null, null, null, msg));
                    return;
                }

                if (BLOQUEADA.matcher(msg).find()) {
                    entries.add(new LogEntry(timestamp, Tipo.BLOQUEADA, null, null, null, null, null, msg));
                    return;
                }

                // fallback: sem parse específico, ainda devolve a linha crua
                entries.add(new LogEntry(timestamp, Tipo.DESCONHECIDO, null, null, null, null, null, msg));
            });
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler log", e);
        }
        return entries;
    }

    // Helpers de filtro
    public static LocalDateTime inicioDoDia(LocalDate d) {
        return d.atStartOfDay();
    }

    public static LocalDateTime fimDoDia(LocalDate d) {
        return d.atTime(23, 59, 59, 999_000_000);
    }
}