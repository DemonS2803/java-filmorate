package ru.yandex.practicum.filmorate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFilmGenreDataException extends RuntimeException {
    public InvalidFilmGenreDataException(final String message) {
        super(message);
    }
}
