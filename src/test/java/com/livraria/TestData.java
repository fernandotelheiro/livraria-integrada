package com.livraria;

import com.livraria.shared.utils.Paths;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class TestData {
    private TestData() {}

    public static void resetarLivros() {
        try (var pw = new PrintWriter(Files.newBufferedWriter(Paths.LIVROS, StandardCharsets.UTF_8))) {
            pw.println("id,titulo,autor,quantidade,preco");
            pw.println("1,O Senhor dos Anéis,J.R.R. Tolkien,3,89.90");
            pw.println("2,Dom Casmurro,Machado de Assis,5,34.50");
            pw.println("3,1984,George Orwell,2,49.90");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao resetar livros.csv para testes", e);
        }
    }

    public static void resetarClientes() {
        try (var pw = new PrintWriter(Files.newBufferedWriter(Paths.CLIENTES, StandardCharsets.UTF_8))) {
            pw.println("id,nome,email");
            pw.println("1,Fulano da Silva,fulano@email.com");
            pw.println("2,Beltrano Souza,beltrano@email.com");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao resetar clientes.csv para testes", e);
        }
    }
}
