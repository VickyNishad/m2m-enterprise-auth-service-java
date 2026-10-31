package com.vickynishad.tech.m2m.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Invalid username or password on /login
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, 
                "Invalid email or password"
        );
        pd.setTitle("Authentication Failure");
        pd.setType(URI.create("https://api.vickynishad.tech/errors/unauthorized"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 2. Token replay / compromise attack detected during refresh token rotation
    @ExceptionHandler(SecurityBreachException.class)
    public ProblemDetail handleSecurityBreach(SecurityBreachException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, 
                ex.getMessage()
        );
        pd.setTitle("Security Policy Violation");
        pd.setType(URI.create("https://api.vickynishad.tech/errors/token-compromise"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 3. User not found
    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUserNotFound(UsernameNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, 
                ex.getMessage()
        );
        pd.setTitle("Resource Not Found");
        pd.setType(URI.create("https://api.vickynishad.tech/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 4. Bean Validation failures (@NotBlank, @Email, @Size)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, 
                "Input payload failed validation"
        );
        pd.setTitle("Validation Failure");
        pd.setType(URI.create("https://api.vickynishad.tech/errors/validation"));
        pd.setProperty("timestamp", Instant.now());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        pd.setProperty("errors", fieldErrors);

        return pd;
    }

    // 5. Business logic errors (e.g. duplicate email registration)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, 
                ex.getMessage()
        );
        pd.setTitle("Bad Request");
        pd.setType(URI.create("https://api.vickynishad.tech/errors/bad-request"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 6. Generic unhandled server exceptions
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected server error occurred"
        );
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("https://api.vickynishad.tech/errors/internal-server-error"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}