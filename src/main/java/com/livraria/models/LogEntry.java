package com.livraria.models;

import java.time.LocalDateTime;

public class LogEntry {
    private final LocalDateTime timestamp;
    private final String tipo;           // COMPRA, CRIACAO, ATUALIZACAO, EXCLUSAO, BLOQUEADA
    private final String cliente;
    private final String livro;
    private final Integer quantidade;
    private final Integer estoqueAntes;
    private final Integer estoqueDepois;
    private final String mensagem;

    public LogEntry(LocalDateTime timestamp, String tipo, String cliente, String livro,
                    Integer quantidade, Integer estoqueAntes, Integer estoqueDepois,
                    String mensagem) {
        this.timestamp = timestamp;
        this.tipo = tipo;
        this.cliente = cliente;
        this.livro = livro;
        this.quantidade = quantidade;
        this.estoqueAntes = estoqueAntes;
        this.estoqueDepois = estoqueDepois;
        this.mensagem = mensagem;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getTipo() { return tipo; }
    public String getCliente() { return cliente; }
    public String getLivro() { return livro; }
    public Integer getQuantidade() { return quantidade; }
    public Integer getEstoqueAntes() { return estoqueAntes; }
    public Integer getEstoqueDepois() { return estoqueDepois; }
    public String getMensagem() { return mensagem; }
}
