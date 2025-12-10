package com.livraria.repositories;

import com.livraria.models.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    List<Cliente> listar();
    void salvarTodos(List<Cliente> clientes);
    Optional<Cliente> buscarPorId(int id);
}
