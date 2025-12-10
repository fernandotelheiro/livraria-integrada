package com.livraria.utils;

import com.livraria.shared.utils.Paths;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class LoggerUtils {
    public void registrar(String mensagem) {
        try {
            // garante que a pasta do log exista
            if (Paths.LOG.getParent() != null) {
                Files.createDirectories(Paths.LOG.getParent());
            }
            try (FileWriter fw = new FileWriter(Paths.LOG.toFile(), true)) {
                fw.write(LocalDateTime.now() + " - " + mensagem + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever log", e);
        }
    }
}
