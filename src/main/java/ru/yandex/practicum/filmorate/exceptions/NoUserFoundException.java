package ru.yandex.practicum.filmorate.exceptions;

import ru.yandex.practicum.filmorate.model.User;

public class NoUserFoundException extends RuntimeException {

    private User missedUser;
    public NoUserFoundException(String message) {
        super(message);
    }
}
