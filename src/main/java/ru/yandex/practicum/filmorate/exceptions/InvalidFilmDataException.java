package ru.yandex.practicum.filmorate.exceptions;

public class InvalidFilmDataException extends RuntimeException {
    public InvalidFilmDataException(final String message) {
        super(message);
    }
}
