package com.reliaquest.api.controller;

import com.reliaquest.api.exception.ValidationException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class EmployeeControllerAdvice {

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    protected ResponseEntity<?> handleNotFound(HttpClientErrorException.NotFound ex) {
        log.error("Error handling web request.", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Entity not found"));
    }

    @ExceptionHandler(WebClientResponseException.NotFound.class)
    protected ResponseEntity<?> handleExternalResourceNotFound(WebClientResponseException.NotFound ex) {
        log.error("Error handling web request.", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Entity not found"));
    }

    @ExceptionHandler(WebClientResponseException.class)
    protected ResponseEntity<?> handleExternalServiceException(WebClientResponseException ex) {
        log.error("Error handling web request.", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "External Service error"));
    }



    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<?> handleValidation(IllegalArgumentException ex) {
        log.error("Error handling web request.", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
    protected ResponseEntity<?> handleProcessingErrors(HttpServerErrorException.InternalServerError ex) {
        log.error("Error handling web request.", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal Server Error"));
    }


    @ExceptionHandler(ValidationException.class)
    protected ResponseEntity<?> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }


    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex) {
        log.error("Resource not found.", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Resource not found"));
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Retries exhausted")) {
            log.error("Retries exhausted for external service call.", ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Service temporarily unavailable - too many requests"));
        }
        // Let other RuntimeExceptions fall through to general handler
        throw ex;
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleGeneral(Exception ex) {
        log.error("Error handling web request.", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }
}
