package ru.practicum.exception;

public class AllreadyExistsException extends RuntimeException {
    public AllreadyExistsException(String message) {
        super(message);
    }
}
