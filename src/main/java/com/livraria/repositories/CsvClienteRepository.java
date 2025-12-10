package com.livraria.repositories;

import com.livraria.models.Cliente;
import com.livraria.shared.utils.Paths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvClienteRepository implements ClienteRepository {

    @Override
    public List<Cliente> listar() {
        var lista = new ArrayList<Cliente>();
        try (BufferedReader br = Files.newBufferedReader(Paths.CLIENTES, StandardCharsets.UTF_8)) {
            String line = br.readLine(); // cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] c = line.split(",", -1);
                if (c.length < 3) continue;

                int id = Integer.parseInt(c[0].trim());
                String nome = c[1].trim();
                String email = c[2].trim();

                lista.add(new Cliente(id, nome, email));
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro lendo clientes.csv", e);
        }
        return lista;
    }

    @Override
    public void salvarTodos(List<Cliente> clientes) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.CLIENTES, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println("id,nome,email");
            for (var c : clientes) {
                String nome  = c.getNome()  == null ? "" : c.getNome().trim();
                String email = c.getEmail() == null ? "" : c.getEmail().trim();
                pw.printf("%d,%s,%s%n", c.getId(), nome, email);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro escrevendo clientes.csv", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return listar().stream().filter(c -> c.getId() == id).findFirst();
    }
}
