package com.livraria.support.services;

import com.livraria.support.models.Ticket;
import com.livraria.support.models.TicketPrioridade;
import com.livraria.support.models.TicketStatus;
import com.livraria.support.repositories.CsvTicketRepository;
import com.livraria.support.repositories.TicketRepository;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;

import java.util.List;
import java.util.regex.Pattern;

public class TicketService {

    // Regex simples só para garantir formato "sad@dasd.sad"
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final TicketRepository repo;

    public TicketService() {
        this(new CsvTicketRepository());
    }

    public TicketService(TicketRepository repo) {
        this.repo = repo;
    }

    // ----------------------------------------------------
    // -----------------------------------------------------------Operações principais
    // ----------------------------------------------------

    public List<Ticket> listar() {
        return repo.listar();
    }

    public Ticket buscarPorId(int id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new NotFoundResponse("Ticket " + id + " não encontrado."));
    }

    public Ticket criar(String titulo,
                        String descricao,
                        String emailCliente,
                        String prioridadeRaw,
                        String statusRaw) {

        // Fail early: validações basic
        validarTitulo(titulo);
        validarEmail(emailCliente);

        TicketPrioridade prioridade = parsePrioridade(prioridadeRaw);
        TicketStatus status = parseStatus(
                (statusRaw == null || statusRaw.isBlank()) ? "ABERTO" : statusRaw
        );

        Ticket ticket = new Ticket(
                0,
                titulo.trim(),
                descricao != null ? descricao.trim() : "",
                emailCliente.trim(),
                prioridade,
                status
        );

        return repo.criar(ticket);
    }

    public Ticket atualizar(int id,
                            String titulo,
                            String descricao,
                            String emailCliente,
                            String prioridadeRaw,
                            String statusRaw) {

        Ticket existente = buscarPorId(id); // dispara 404 se não existir

        validarTitulo(titulo);
        validarEmail(emailCliente);

        TicketPrioridade prioridade =
                parsePrioridade(prioridadeRaw != null ? prioridadeRaw : existente.getPrioridade().name());

        TicketStatus status =
                parseStatus(statusRaw != null ? statusRaw : existente.getStatus().name());

        existente.setTitulo(titulo.trim());
        existente.setDescricao(descricao != null ? descricao.trim() : "");
        existente.setEmailCliente(emailCliente.trim());
        existente.setPrioridade(prioridade);
        existente.setStatus(status);

        return repo.atualizar(existente);
    }

    public void excluir(int id) {
        buscarPorId(id); // garante 404 se não existir
        repo.excluir(id);
    }

    // ----------------------------------------------------
    // ----------------------------------------------------------Validações (fail early)
    // ----------------------------------------------------

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new BadRequestResponse("Título é obrigatório.");
        }
    }

    private void validarEmail(String emailCliente) {
        if (emailCliente == null || emailCliente.isBlank()) {
            throw new BadRequestResponse("E-mail do cliente é obrigatório.");
        }
        String email = emailCliente.trim();
        if (!EMAIL_REGEX.matcher(email).matches()) {
            throw new BadRequestResponse("E-mail do cliente é inválido.");
        }
    }

    // ----------------------------------------------------
    //------------------------------------------------------------ Helpers de parsing (prioridade/status)
    // ----------------------------------------------------

    private TicketPrioridade parsePrioridade(String prioridadeRaw) {
        if (prioridadeRaw == null || prioridadeRaw.isBlank()) {
            return TicketPrioridade.MEDIA;
        }

        String valor = prioridadeRaw.trim().toUpperCase();
        try {
            return TicketPrioridade.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse(
                    "Prioridade inválida. Use BAIXA, MEDIA, ALTA ou CRITICA."
            );
        }
    }

    private TicketStatus parseStatus(String statusRaw) {
        if (statusRaw == null || statusRaw.isBlank()) {
            return TicketStatus.ABERTO;
        }

        String valor = statusRaw.trim().toUpperCase();
        try {
            return TicketStatus.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse(
                    "Status inválido. Use ABERTO, EM_ANDAMENTO, RESOLVIDO ou CANCELADO."
            );
        }
    }
}
