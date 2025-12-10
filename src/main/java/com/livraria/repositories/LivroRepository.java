package com.livraria.repositories;

import com.livraria.models.Livro;
import com.livraria.shared.utils.Paths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LivroRepository {

    public List<Livro> listar() {
        var lista = new ArrayList<Livro>();
        try (BufferedReader br = Files.newBufferedReader(Paths.LIVROS, StandardCharsets.UTF_8)) {
            String line = br.readLine(); // cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] c = line.split(",", -1);
                if (c.length < 5) continue;

                int id          = Integer.parseInt(c[0].trim());
                String titulo   = c[1].trim();
                String autor    = c[2].trim();
                int quantidade  = Integer.parseInt(c[3].trim());
                double preco    = Double.parseDouble(c[4].trim());

                lista.add(new Livro(id, titulo, autor, quantidade, preco));
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro lendo livros.csv", e);
        }
        return lista;
    }

    public void salvarTodos(List<Livro> livros) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.LIVROS, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println("id,titulo,autor,quantidade,preco");
            for (var l : livros) {
                String titulo = l.getTitulo() == null ? "" : l.getTitulo().trim();
                String autor  = l.getAutor()  == null ? "" : l.getAutor().trim();
                // força ponto como separador decimal
                pw.printf(Locale.US, "%d,%s,%s,%d,%.2f%n",
                        l.getId(), titulo, autor, l.getQuantidade(), l.getPreco());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro escrevendo livros.csv", e);
        }
    }

    public Optional<Livro> buscarPorId(int id) {
        return listar().stream().filter(l -> l.getId() == id).findFirst();
    }

    /** Busca por título (case-insensitive e trim). */
    public Optional<Livro> buscarPorTitulo(String titulo) {
        String alvo = norm(titulo);
        return listar().stream()
                .filter(l -> norm(l.getTitulo()).equals(alvo))
                .findFirst();
    }

    /** Busca por TÍTULO + AUTHOR (case-insensitive, trim e colapso de espaços). */
    public Optional<Livro> buscarPorTituloEAutor(String titulo, String autor) {
        String t = norm(titulo);
        String a = norm(autor);
        return listar().stream()
                .filter(l -> norm(l.getTitulo()).equals(t) && norm(l.getAutor()).equals(a))
                .findFirst();
    }

    // --- util
    private static String norm(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
