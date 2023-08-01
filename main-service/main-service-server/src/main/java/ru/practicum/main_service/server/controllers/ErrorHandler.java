package ru.practicum.main_service.server.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.main_service.server.utility.errors.BadRequestError;
import ru.practicum.main_service.server.utility.errors.BaseError;
import ru.practicum.main_service.server.utility.errors.ConflictError;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> getResponseEntity(Throwable e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (e.getClass() == MissingServletRequestParameterException.class) {
            status = HttpStatus.BAD_REQUEST;
        } else if (e.getClass() == BadRequestError.class) {
            status = HttpStatus.BAD_REQUEST;
        } else if (e.getClass() == ConflictError.class) {
            status = HttpStatus.CONFLICT;
        } else if (e.getClass() == NotFoundError.class) {
            status = HttpStatus.NOT_FOUND;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("error_class", e.getClass().getSimpleName());
        if (e instanceof BaseError) {
            BaseError error = (BaseError) e;
            if (error.getData() != null) {
                JSONObject jsonData = new JSONObject(error);
                response.put("error_data", jsonData.toString());
            }
        }
        return new ResponseEntity<>(response, status);
    }
}
