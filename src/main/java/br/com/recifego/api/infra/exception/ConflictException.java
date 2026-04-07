package br.com.recifego.api.infra.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
    
}
