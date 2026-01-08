package ru.itmo.is.exception;

public class InvalidDataStateException extends RuntimeException {
    public InvalidDataStateException(String message) {
        super(message);
    }
}
