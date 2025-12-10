package com.livraria.models;

public class Livro {
    private int id;
    private String titulo;
    private String autor;
    private int quantidade;
    private double preco;

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

    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}
