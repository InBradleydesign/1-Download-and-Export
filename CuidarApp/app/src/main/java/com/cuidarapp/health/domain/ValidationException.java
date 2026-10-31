package com.cuidarapp.health.domain;

/** Erro de validação com mensagem simples em português para mostrar ao usuário. */
public class ValidationException extends Exception {

    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
