package ru.practicum.stats.server.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<Map<String, String>> getResponseEntity(Throwable e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Тут возможно будет проверка на классы ошибок, чтобы менять код ответа.

        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("error_class", e.getClass().getSimpleName());
        return new ResponseEntity<>(response, status);
    }
}
