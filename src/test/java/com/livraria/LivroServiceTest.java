package com.livraria;

import com.livraria.services.LivroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LivroServiceTest {

    @BeforeEach
    void setup() {
        TestData.resetarLivros();
        TestData.resetarClientes();
    }

    @Test
    void compraValida_deveAtualizarEstoque() {
        var service = new LivroService();
        assertDoesNotThrow(() -> service.comprar("1984", "Fulano da Silva", 1));
    }

    @Test
    void compraComEstoqueInsuficiente_deveFalhar() {
        var service = new LivroService();
        var e = assertThrows(RuntimeException.class,
                () -> service.comprar("1984", "Fulano", 999));
        assertTrue(e.getMessage().toLowerCase().contains("estoque"));
    }
}
