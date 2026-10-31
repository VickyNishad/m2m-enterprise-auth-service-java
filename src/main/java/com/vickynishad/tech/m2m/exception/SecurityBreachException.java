package com.vickynishad.tech.m2m.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class SecurityBreachException extends RuntimeException {

    public SecurityBreachException(String message) {
        super(message);
    }

    public SecurityBreachException(String message, Throwable cause) {
        super(message, cause);
    }
}