package ru.practicum.main_service.server.utility.errors;

public class ConflictError extends BaseError{
    public ConflictError(String message) {
        super(message);
    }

    public ConflictError(String message, Object data) {
        super(message, data);
    }
}
