package ru.yandex.practicum.filmorate.exceptions;

public class NoFilmFoundException extends RuntimeException {
    public NoFilmFoundException(final String message) {
        super(message);
    }
}
