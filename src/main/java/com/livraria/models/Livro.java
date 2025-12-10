package com.livraria.models;

public final class Livro {

    private final int id;
    private final String titulo;
    private final String autor;
    private final int quantidade;
    private final double preco;

    public Livro(int id, String titulo, String autor, int quantidade, double preco) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public int getQuantidade() { return quantidade; }
    public double getPreco() { return preco; }

    public Livro withQuantidade(int novaQuantidade) {
        return new Livro(
                this.id,
                this.titulo,
                this.autor,
                novaQuantidade,
                this.preco
        );
    }

    public Livro withPreco(double novoPreco) {
        return new Livro(
                this.id,
                this.titulo,
                this.autor,
                this.quantidade,
                novoPreco
        );
    }
}
