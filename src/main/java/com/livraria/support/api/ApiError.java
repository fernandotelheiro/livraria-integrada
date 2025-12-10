package com.livraria.support.api;

/**
 * Modelo padrão de erro retornado pela API.
 */
public record ApiError(
        String type,    //  ex.: "VALIDATION_ERROR", "NOT_FOUND"
        String message  // mensagem legível para o user
) {}
