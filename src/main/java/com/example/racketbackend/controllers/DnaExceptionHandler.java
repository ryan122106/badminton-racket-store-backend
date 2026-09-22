package com.example.racketbackend.controllers;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class DnaExceptionHandler {

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<Map<String, String>>
    handleBadRequest(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(
            OptimisticLockingFailureException.class
    )
    public ResponseEntity<Map<String, String>>
    handleStockConflict() {
        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        Map.of(
                                "message",
                                "Stock changed at the same time. Reload and try again."
                        )
                );
    }
}