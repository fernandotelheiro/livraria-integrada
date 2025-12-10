package com.livraria.utils;

import com.livraria.shared.utils.Paths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class SeedData {
    private SeedData() {}

    public static void ensureFiles() {
        try {
            if (!Files.exists(Paths.LIVROS)) {
                Files.writeString(Paths.LIVROS, "id,titulo,autor,quantidade,preco\n", StandardCharsets.UTF_8);
            }
            if (!Files.exists(Paths.CLIENTES)) {
                Files.writeString(Paths.CLIENTES, "id,nome,email\n", StandardCharsets.UTF_8);
            }
            if (!Files.exists(Paths.LOG)) {
                Files.createFile(Paths.LOG);
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao criar arquivos de dados em " + Paths.DATA_DIR, e);
        }
    }
}
