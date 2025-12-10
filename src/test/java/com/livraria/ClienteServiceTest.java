package com.livraria;

import com.livraria.models.Cliente;
import com.livraria.services.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClienteServiceTest {

    @BeforeEach
    void setup() {
        TestData.resetarClientes();
    }

    @Test
    void criarCliente_deveAtribuirId() {
        var service = new ClienteService();
        var criado = service.criar(new Cliente(0, "Novo Cliente", "novo@email"));
        assertTrue(criado.getId() > 0);
    }
}
