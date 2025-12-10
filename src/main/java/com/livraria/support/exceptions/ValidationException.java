package com.livraria.support.exceptions;

/**
 * Erros de validação de entrada (dados inválidos do user).
 * Deve resultar em HTTP 400.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
