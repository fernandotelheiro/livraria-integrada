package com.livraria.support.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Paths {

    private Paths() {
        // utilitário(não deve ser instanciado)
    }

    /**
     * Retorna (e cria se não existir) o diretório "data" na raiz do projeto.
     */
    public static Path dataDir() {
        Path dir = Path.of("data");
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("Não foi possível criar diretório de dados: " + dir, e);
            }
        }
        return dir;
    }

    /**
     * Caminho completo para o arquivo tickets.csv dentro de data/.
     */
    public static Path ticketsCsv() {
        return dataDir().resolve("tickets.csv");
    }
}
