package ru.yandex.practicum.filmorate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoFilmFoundException extends RuntimeException {
    public NoFilmFoundException(final String message) {
        super(message);
    }
}
