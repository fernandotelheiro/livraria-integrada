package com.livraria.support.repositories;

import com.livraria.support.models.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {

    List<Ticket> listar();

    Optional<Ticket> buscarPorId(int id);

    Ticket criar(Ticket ticket);

    Ticket atualizar(Ticket ticket);

    void excluir(int id);
}
