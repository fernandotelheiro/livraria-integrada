package com.livraria.models;

public final class Cliente {

    private final int id;
    private final String nome;
    private final String email;

    public Cliente(int id, String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente não pode ser vazio");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email do cliente não pode ser vazio");
        }
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Cria um novo Cliente com nome e email atualizados,
     * mantendo o mesmo id.
     */
    public Cliente comDadosAtualizados(String novoNome, String novoEmail) {
        return new Cliente(this.id, novoNome, novoEmail);
    }
}
