package com.livraria.support.models;

public class Ticket {

    private int id;
    private String titulo;
    private String descricao;
    private String emailCliente;
    private TicketPrioridade prioridade;
    private TicketStatus status;

    public Ticket() {
        // Construtor vazio para Jackson/Javalin
    }

    public Ticket(int id,
                  String titulo,
                  String descricao,
                  String emailCliente,
                  TicketPrioridade prioridade,
                  TicketStatus status) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.emailCliente = emailCliente;
        this.prioridade = prioridade;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public TicketPrioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(TicketPrioridade prioridade) {
        this.prioridade = prioridade;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
