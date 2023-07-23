package ru.practicum.main_service.server.utility.errors;

public class NotFoundError extends BaseError {
    public NotFoundError(String message) {
        super(message);
    }

    public NotFoundError(String message, Object data) {
        super(message, data);
    }
}
