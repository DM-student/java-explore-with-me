package ru.practicum.main_service.server.utility.errors;

public class BadRequestError extends BaseError{
    public BadRequestError(String message) {
        super(message);
    }

    public BadRequestError(String message, Object data) {
        super(message, data);
    }
}
