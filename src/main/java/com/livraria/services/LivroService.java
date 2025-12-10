package com.livraria.services;

import com.livraria.models.Livro;
import com.livraria.repositories.CsvLivroRepository;
import com.livraria.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LivroService {

    private final CsvLivroRepository repo;
    private final LoggerUtils logger;

    public LivroService() {
        this.repo = new CsvLivroRepository();
        this.logger = new LoggerUtils();
    }

    public List<Livro> listar() {
        return repo.listar();
    }

    /**
     * Título e Autor obrigatórios.
     * Se (titulo+autor) já existir, soma quantidade fazendo um merge dos valores;
     * Senão, cria novo registro.
     */
    public Livro criar(Livro novo) {
        var todos = repo.listar();

        String titulo = safeTrim(novo.getTitulo());
        String autor  = safeTrim(novo.getAutor());
        int qtdNova   = Math.max(0, novo.getQuantidade());
        double preco  = novo.getPreco();

        if (isBlank(titulo) || isBlank(autor)) {
            throw new IllegalArgumentException("Título e autor são obrigatórios");
        }

        Livro existente = null;
        String tNorm = norm(titulo);
        String aNorm = norm(autor);
        for (var l : todos) {
            if (norm(l.getTitulo()).equals(tNorm) && norm(l.getAutor()).equals(aNorm)) {
                existente = l;
                break;
            }
        }

        if (existente != null) {
            int antes = existente.getQuantidade();
            int depois = antes + qtdNova;

            // cria nova instância com quantidade atualizada
            Livro atualizado = existente.withQuantidade(depois);

            List<Livro> novaLista = new ArrayList<>();
            for (Livro l : todos) {
                if (l.getId() == existente.getId()) {
                    novaLista.add(atualizado);
                } else {
                    novaLista.add(l);
                }
            }
            repo.salvarTodos(novaLista);

            logger.registrar(String.format(
                    "ATUALIZACAO|acao=MERGE|id=%d|livro=%s|autor=%s|antes=%d|adicionado=%d|depois=%d|preco=%.2f",
                    atualizado.getId(), titulo, autor, antes, qtdNova, depois, atualizado.getPreco()
            ));
            return atualizado;
        }

        int nextId = todos.stream().mapToInt(Livro::getId).max().orElse(0) + 1;
        var livro = new Livro(nextId, titulo, autor, qtdNova, preco);

        var list = new ArrayList<>(todos);
        list.add(livro);
        repo.salvarTodos(list);

        logger.registrar(String.format(
                "CRIACAO|id=%d|livro=%s|autor=%s|qtd=%d|preco=%.2f",
                livro.getId(), livro.getTitulo(), livro.getAutor(), livro.getQuantidade(), livro.getPreco()
        ));
        return livro;
    }

    public Livro atualizar(int id, Livro dados) {
        var todos = repo.listar();
        var list = new ArrayList<Livro>();
        Livro atualizado = null;

        String t = safeTrim(dados.getTitulo());
        String a = safeTrim(dados.getAutor());

        if (isBlank(t) || isBlank(a)) {
            throw new IllegalArgumentException("Título e autor são obrigatórios");
        }

        for (var l : todos) {
            if (l.getId() == id) {
                atualizado = new Livro(
                        id,
                        t,
                        a,
                        dados.getQuantidade(),
                        dados.getPreco()
                );
                list.add(atualizado);
            } else {
                list.add(l);
            }
        }
        if (atualizado == null) {
            throw new RuntimeException("Livro não encontrado");
        }

        repo.salvarTodos(list);
        logger.registrar(String.format(
                "ATUALIZACAO|id=%d|livro=%s|autor=%s|qtd=%d|preco=%.2f",
                id, atualizado.getTitulo(), atualizado.getAutor(),
                atualizado.getQuantidade(), atualizado.getPreco()
        ));
        return atualizado;
    }

    public void excluir(int id) {
        var todos = repo.listar();
        var alvo = repo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        if (alvo.getQuantidade() == 0) {
            logger.registrar(String.format(
                    "BLOQUEADA|acao=EXCLUSAO|id=%d|livro=%s|motivo=ESTOQUE_ZERO",
                    id, alvo.getTitulo()
            ));
            return;
        }

        var list = new ArrayList<Livro>();
        for (var l : todos) {
            if (l.getId() != id) {
                list.add(l);
            }
        }
        repo.salvarTodos(list);

        logger.registrar(String.format(
                "EXCLUSAO|id=%d|livro=%s",
                id, alvo.getTitulo()
        ));
    }

    public void comprar(String tituloLivro, String nomeCliente, int quantidade) {
        var livro = repo.buscarPorTitulo(tituloLivro)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade inválida");
        }

        int anterior = livro.getQuantidade();
        int novo = anterior - quantidade;

        if (novo < 0) {
            throw new RuntimeException("Estoque insuficiente");
        }

        var todos = repo.listar();
        var list = new ArrayList<Livro>();
        for (var l : todos) {
            if (l.getId() == livro.getId()) {
                list.add(l.withQuantidade(novo));
            } else {
                list.add(l);
            }
        }
        repo.salvarTodos(list);

        logger.registrar(String.format(
                "COMPRA|cliente=%s|livro=%s|qtd=%d|antes=%d|depois=%d",
                nomeCliente, livro.getTitulo(), quantidade, anterior, novo
        ));
    }

    // utils
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String norm(String s) {
        if (s == null) return "";
        return s.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
