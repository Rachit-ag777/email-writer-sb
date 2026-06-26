package com.email.writer.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleGeminiError(WebClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        String message = body != null && !body.isBlank()
                ? body
                : ex.getMessage();
        return ResponseEntity.status(ex.getStatusCode()).body("Gemini API error: " + message);
    }
}
