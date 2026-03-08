package ru.yandex.practicum.filmorate.controller;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.yandex.practicum.filmorate.dto.ErrorResponse;
import ru.yandex.practicum.filmorate.exceptions.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmGenreFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("Unexpected error", e);
        return new ResponseEntity<>(
                new ErrorResponse(500, "Internal Server Error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Failed to validate arguments", e);
        return new ResponseEntity<>(
                new ErrorResponse(400, "Failed validation", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<?> handleInvalidUserDataException(InvalidUserDataException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(400, "Invalid user data", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NoUserFoundException.class)
    public ResponseEntity<?> handleNoUserFoundException(NoUserFoundException e) {
        log.warn(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(404, "Failed to create user", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NoFilmFoundException.class)
    public ResponseEntity<?> handleNoFilmFoundException(NoFilmFoundException e) {
        log.warn(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(404, "Failed to create film", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InvalidFilmDataException.class)
    public ResponseEntity<?> handleInvalidFilmDataException(InvalidFilmDataException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(400, "Invalid film data", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @Order(100)
    @ExceptionHandler(NoFilmRatingFoundException.class)
    public ResponseEntity<?> handleNoFilmRatingFoundException(NoFilmRatingFoundException e) {
        log.warn(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(404, "Failed to find film rating", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NoFilmGenreFoundException.class)
    public ResponseEntity<?> handleNoFilmGenreFoundException(NoFilmGenreFoundException e) {
        log.warn(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(404, "Failed to find film genre", e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Failed to validate incoming arguments", e);
        return new ResponseEntity<>(new ErrorResponse(404, "Bad Request", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

}
