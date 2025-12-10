package com.livraria.support.exceptions;

/**
 * Entidade não encontrada (ex: ticket com ID inexistente).
 * Deve resultar em HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
