package ru.yandex.practicum.filmorate.exceptions;

public class NoUserFoundException extends RuntimeException {
    public NoUserFoundException(String message) {
        super(message);
    }
}
