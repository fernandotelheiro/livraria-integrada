package com.livraria.support.repositories;

import com.livraria.support.models.Ticket;
import com.livraria.support.models.TicketPrioridade;
import com.livraria.support.models.TicketStatus;
import com.livraria.shared.utils.Paths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvTicketRepository implements TicketRepository {

    private static final String SEP = ";";

    private Path csvPath() {
        // antes: return Paths.ticketsCsv();
        return Paths.TICKETS;
    }

    @Override
    public synchronized List<Ticket> listar() {
        try {
            Path path = csvPath();
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<Ticket> tickets = new ArrayList<>();

            if (lines.isEmpty()) {
                return tickets;
            }

            // pula cabeçalho
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                String[] cols = line.split(SEP, -1);
                if (cols.length < 6) continue; // ignora linha mal formada

                int id = Integer.parseInt(cols[0]);
                String titulo = cols[1];
                String descricao = cols[2];
                String email = cols[3];
                TicketPrioridade prioridade = TicketPrioridade.valueOf(cols[4]);
                TicketStatus status = TicketStatus.valueOf(cols[5]);

                tickets.add(new Ticket(
                        id,
                        titulo,
                        descricao,
                        email,
                        prioridade,
                        status
                ));
            }

            return tickets;

        } catch (IOException e) {
            // Deixa estourar como RuntimeException -> tratado pelo handler 500 do MainSupport
            throw new RuntimeException("Erro ao ler arquivo de tickets", e);
        }
    }

    @Override
    public synchronized Optional<Ticket> buscarPorId(int id) {
        return listar().stream()
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    @Override
    public synchronized Ticket criar(Ticket novo) {
        List<Ticket> todos = listar();
        int nextId = todos.stream()
                .mapToInt(Ticket::getId)
                .max()
                .orElse(0) + 1;

        novo.setId(nextId);
        todos.add(novo);
        persist(todos);
        return novo;
    }

    @Override
    public synchronized Ticket atualizar(Ticket atualizado) {
        List<Ticket> todos = listar();
        boolean found = false;

        for (int i = 0; i < todos.size(); i++) {
            if (todos.get(i).getId() == atualizado.getId()) {
                todos.set(i, atualizado);
                found = true;
                break;
            }
        }

        if (!found) {
            // Service já garante 404 antes, mas aqui funciona como uma segurança adicional
            throw new RuntimeException("Ticket " + atualizado.getId() + " não encontrado para atualização");
        }

        persist(todos);
        return atualizado;
    }

    @Override
    public synchronized void excluir(int id) {
        List<Ticket> todos = listar();
        boolean removed = todos.removeIf(t -> t.getId() == id);
        persist(todos);
    }

    // -------------------------------------- helpers -----------------

    private void persist(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        lines.add("id;titulo;descricao;emailCliente;prioridade;status");

        for (Ticket t : tickets) {
            lines.add(String.join(SEP,
                    Integer.toString(t.getId()),
                    escape(t.getTitulo()),
                    escape(t.getDescricao()),
                    escape(t.getEmailCliente()),
                    t.getPrioridade().name(),
                    t.getStatus().name()
            ));
        }

        try {
            Path path = csvPath();
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo de tickets", e);
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        // evita bagunçar o CSV se tiver ';' no meio do texto
        return value.replace(SEP, " ");
    }
}
