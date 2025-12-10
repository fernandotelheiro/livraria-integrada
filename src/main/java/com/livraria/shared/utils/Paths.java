package com.livraria.shared.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Paths {

    public static final Path DATA_DIR;
    public static final Path LIVROS;
    public static final Path CLIENTES;
    public static final Path LOG;
    public static final Path TICKETS;

    static {
        // 1) Permite override via VM option:
        //    Define onde ficam os arquivos de dados (CSV e log)
        String override = System.getProperty("livraria.data.dir");

        // 2) Default = pasta 'data' dentro do diretório de trabalho
        Path base = (override != null && !override.isBlank())
                ? Path.of(override)
                : Path.of(System.getProperty("user.dir")).resolve("data");

        DATA_DIR = base.toAbsolutePath();
        LIVROS   = DATA_DIR.resolve("livros.csv");
        CLIENTES = DATA_DIR.resolve("clientes.csv");
        LOG      = DATA_DIR.resolve("log_acoes.txt");
        TICKETS  = DATA_DIR.resolve("tickets.csv");

        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de dados: " + DATA_DIR, e);
        }
    }

    private Paths() {}
}
