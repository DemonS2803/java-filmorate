package ru.yandex.practicum.filmorate.exceptions;

public class InvalidUserDataException extends RuntimeException {
    public InvalidUserDataException(final String message) {
        super(message);
    }
}
