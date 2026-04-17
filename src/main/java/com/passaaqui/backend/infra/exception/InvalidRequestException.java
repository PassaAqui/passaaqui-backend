package com.passaaqui.backend.infra.exception;

public class InvalidRequestException extends RuntimeException {
    
    public InvalidRequestException(String message) {
        super(message);
    }

}
