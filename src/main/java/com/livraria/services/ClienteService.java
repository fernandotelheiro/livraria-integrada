package com.livraria.services;

import com.livraria.models.Cliente;
import com.livraria.repositories.CsvClienteRepository;
import com.livraria.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.Locale;

public class ClienteService {
    private final CsvClienteRepository repo;
    private final LoggerUtils logger;

    public ClienteService() {
        this.repo = new CsvClienteRepository();
        this.logger = new LoggerUtils();
    }

    public java.util.List<Cliente> listar() {
        return repo.listar();
    }

    /** Criação cliente novo. Bloqueia if (nome + email) já existir (case-insensitive, trim). */
    public Cliente criar(Cliente novo) {
        String nomeBruto  = novo.getNome();
        String emailBruto = novo.getEmail();

        if (isBlank(nomeBruto) || isBlank(emailBruto)) {
            throw new IllegalArgumentException("Nome e e-mail são obrigatórios");
        }

        String nomeKey  = norm(nomeBruto);
        String emailKey = norm(emailBruto);

        var existentes = repo.listar();
        boolean duplicado = existentes.stream().anyMatch(c ->
                norm(c.getNome()).equals(nomeKey) && norm(c.getEmail()).equals(emailKey)
        );

        if (duplicado) {
            logger.registrar(String.format(
                    "BLOQUEADA|acao=CRIACAO_CLIENTE|motivo=DUPLICADO|nome=%s|email=%s",
                    nomeBruto.trim(), emailBruto.trim()
            ));
            throw new IllegalArgumentException("Cliente já existente, impossível realizar novo cadastro");
        }

        int nextId = existentes.stream().mapToInt(Cliente::getId).max().orElse(0) + 1;
        var cliente = new Cliente(nextId, nomeBruto.trim(), emailBruto.trim());

        var list = new ArrayList<>(existentes);
        list.add(cliente);
        repo.salvarTodos(list);

        logger.registrar(String.format(
                "CRIACAO_CLIENTE|id=%d|nome=%s|email=%s",
                cliente.getId(), cliente.getNome(), cliente.getEmail()
        ));
        return cliente;
    }

    public Cliente atualizar(int id, Cliente dados) {
        var todos = repo.listar();
        var list = new ArrayList<Cliente>();
        Cliente atualizado = null;

        for (var c : todos) {
            if (c.getId() == id) {
                atualizado = new Cliente(
                        id,
                        safeTrim(dados.getNome()),
                        safeTrim(dados.getEmail())
                );
                list.add(atualizado);
            } else {
                list.add(c);
            }
        }
        if (atualizado == null) throw new RuntimeException("Cliente não encontrado");

        repo.salvarTodos(list);
        logger.registrar(String.format("ATUALIZACAO_CLIENTE|id=%d", id));
        return atualizado;
    }

    public void excluir(int id) {
        var todos = repo.listar();
        var list = new ArrayList<Cliente>();
        boolean removed = false;

        for (var c : todos) {
            if (c.getId() != id) list.add(c);
            else removed = true;
        }
        if (!removed) throw new RuntimeException("Cliente não encontrado");

        repo.salvarTodos(list);
        logger.registrar(String.format("EXCLUSAO_CLIENTE|id=%d", id));
    }

    // ---- utils

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    /** normalização para comparação (ignorando caixa e espaços) */
    private static String norm(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
