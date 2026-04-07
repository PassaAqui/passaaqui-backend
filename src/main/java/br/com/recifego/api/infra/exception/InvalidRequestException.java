package br.com.recifego.api.infra.exception;

public class InvalidRequestException extends RuntimeException {
    
    public InvalidRequestException(String message) {
        super(message);
    }

}
