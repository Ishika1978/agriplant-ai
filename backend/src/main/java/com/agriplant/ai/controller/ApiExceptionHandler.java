package com.agriplant.ai.controller;

import com.agriplant.ai.service.UnclearPredictionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    /** Low-confidence prediction: a real answer, not a server fault. */
    @ExceptionHandler(UnclearPredictionException.class)
    public ResponseEntity<Map<String, Object>> onUnclear(UnclearPredictionException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "unclear_prediction", "message", ex.getMessage()));
    }

    /** AI service unreachable or model not yet trained. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> onIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "prediction_unavailable", "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> onBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "bad_request", "message", ex.getMessage()));
    }
}
