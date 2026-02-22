package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.dto.ErrorResponse;
import ru.yandex.practicum.filmorate.exceptions.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerController {
    
    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<?> handleInvalidUserDataException(final InvalidUserDataException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(400, "Invalid user data", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NoUserFoundException.class)
    public ResponseEntity<?> handleNoUserFoundException(final NoUserFoundException e) {
        log.warn(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(404, "Failed to create user", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    // Можно создать общие исключения NoEntityFound/InvalidEntityData, но пока что можно и без них обойтись

    @ExceptionHandler(InvalidFilmDataException.class)
    public ResponseEntity<?> handleInvalidFilmDataException(final InvalidFilmDataException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(400, "Invalid film data", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NoFilmFoundException.class)
    public ResponseEntity<?> handleNoFilmFoundException(final NoFilmFoundException e) {
        log.warn(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(404, "Failed to create film", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

}
