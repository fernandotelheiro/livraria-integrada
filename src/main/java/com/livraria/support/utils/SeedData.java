package com.livraria.support.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SeedData {

    private SeedData() {
        // utilitário (não deve ser instanciado)
    }

    /**
     * Garante que o arquivo tickets.csv exista.
     * Se não existir, cria com um cabeçalho e um registro de exemplo.
     */
    public static void ensureTicketsCsvExists() {
        Path csv = Paths.ticketsCsv();

        if (Files.exists(csv)) {
            return; // já existe, não faz nada
        }

        List<String> lines = List.of(
                "id;titulo;descricao;emailCliente;prioridade;status",
                "1;Exemplo de ticket;Descrição de exemplo;cliente@example.com;MEDIA;ABERTO"
        );

        try {
            Files.createDirectories(csv.getParent());
            Files.write(csv, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar arquivo CSV inicial: " + csv, e);
        }
    }
}
