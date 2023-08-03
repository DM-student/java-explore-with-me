package ru.practicum.main_service.server.utility.errors;

public class BaseError extends RuntimeException {
    private final Object data;

    public Object getData() {
        return data;
    }

    public BaseError(String message) {
        super(message);
        data = null;
    }

    public BaseError(String message, Object data) {
        super(message);
        this.data = data;
    }
}
