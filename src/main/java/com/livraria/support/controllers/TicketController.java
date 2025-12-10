package com.livraria.support.controllers;

import com.livraria.support.exceptions.NotFoundException;
import com.livraria.support.exceptions.ValidationException;
import com.livraria.support.models.Ticket;
import com.livraria.support.services.TicketService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TicketController {

    private final TicketService service = new TicketService();

    // conjunto de valores permitidos (validacão simples)
    private static final Set<String> PRIORIDADES_VALIDAS = Set.of(
            "BAIXA", "MEDIA", "ALTA", "CRITICA"
    );

    private static final Set<String> STATUS_VALIDOS = Set.of(
            "ABERTO", "EM_ANDAMENTO", "RESOLVIDO", "CANCELADO"
    );

    // Regex de email bem simples
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // DTO usado para ler JSON do body
    public static class NovoTicketDTO {
        public String titulo;
        public String descricao;
        public String emailCliente;
        public String prioridade;
        public String status;
    }

    // =============================== ENDPOINTS ======

    public void listar(Context ctx) {
        List<Ticket> tickets = service.listar();
        ctx.json(tickets);
    }

    public void obter(Context ctx) {
        int id = parseId(ctx);

        Ticket ticket = service.buscarPorId(id);
        if (ticket == null) {
            throw new NotFoundException("Ticket com id %d não encontrado".formatted(id));
        }

        ctx.json(ticket);
    }

    public void criar(Context ctx) {
        NovoTicketDTO dto = ctx.bodyAsClass(NovoTicketDTO.class);
        validarDto(dto);

        Ticket criado = service.criar(
                dto.titulo,
                dto.descricao,
                dto.emailCliente,
                dto.prioridade,
                dto.status
        );

        ctx.status(201).json(criado);
    }

    public void atualizar(Context ctx) {
        int id = parseId(ctx);
        NovoTicketDTO dto = ctx.bodyAsClass(NovoTicketDTO.class);
        validarDto(dto);

        // garantir que existe antes de atualizar
        Ticket existente = service.buscarPorId(id);
        if (existente == null) {
            throw new NotFoundException("Ticket com id %d não encontrado".formatted(id));
        }

        Ticket atualizado = service.atualizar(
                id,
                dto.titulo,
                dto.descricao,
                dto.emailCliente,
                dto.prioridade,
                dto.status
        );

        ctx.json(atualizado);
    }

    public void excluir(Context ctx) {
        int id = parseId(ctx);

        Ticket existente = service.buscarPorId(id);
        if (existente == null) {
            throw new NotFoundException("Ticket com id %d não encontrado".formatted(id));
        }

        service.excluir(id);
        ctx.status(204).json(Map.of("ok", true));
    }

    // ============================= HELPERS DE VALIDACAO ======

    private int parseId(Context ctx) {
        String raw = ctx.pathParam("id");
        try {
            int id = Integer.parseInt(raw);
            if (id <= 0) {
                throw new ValidationException("Id deve ser positivo.");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ValidationException("Id inválido: " + raw);
        }
    }

    private void validarDto(NovoTicketDTO dto) {
        if (dto == null) {
            throw new ValidationException("Corpo da requisição é obrigatório.");
        }

        if (isBlank(dto.titulo)) {
            throw new ValidationException("Título é obrigatório.");
        }

        if (isBlank(dto.descricao)) {
            throw new ValidationException("Descrição é obrigatória.");
        }

        if (isBlank(dto.emailCliente)) {
            throw new ValidationException("E-mail do cliente é obrigatório.");
        }

        if (!EMAIL_PATTERN.matcher(dto.emailCliente.trim()).matches()) {
            throw new ValidationException("E-mail do cliente inválido.");
        }

        if (isBlank(dto.prioridade)) {
            throw new ValidationException("Prioridade é obrigatória.");
        }
        String prioridadeUpper = dto.prioridade.trim().toUpperCase();
        if (!PRIORIDADES_VALIDAS.contains(prioridadeUpper)) {
            throw new ValidationException(
                    "Prioridade inválida. Valores permitidos: " + PRIORIDADES_VALIDAS
            );
        }

        if (isBlank(dto.status)) {
            throw new ValidationException("Status é obrigatório.");
        }
        String statusUpper = dto.status.trim().toUpperCase();
        if (!STATUS_VALIDOS.contains(statusUpper)) {
            throw new ValidationException(
                    "Status inválido. Valores permitidos: " + STATUS_VALIDOS
            );
        }

        // normaliza para upperscale antes de mandar para o service
        dto.prioridade = prioridadeUpper;
        dto.status = statusUpper;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
