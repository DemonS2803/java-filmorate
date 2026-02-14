package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private Long userCurrentId = 1L;

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody UserDto incomingUserDto) {
        User user = User.of(incomingUserDto);
        if (user.getId() == null) {
            log.info("User id is empty. Set new id: {}", userCurrentId);
            user.setId(userCurrentId++);
        } else if (users.containsKey(user.getId()) || user.getId() < userCurrentId) {
            log.warn("User with id {} already exists. Set new id: {}", user.getId(), userCurrentId);
            user.setId(userCurrentId++);
        }
        log.info("User added new user: {}", user);
        users.put(user.getId(), user);
        userCurrentId = Math.max(userCurrentId, user.getId() + 1);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody UserDto incomingUserDto) {
        log.info("User want update user: {}", incomingUserDto);
        User updatedUser = User.of(incomingUserDto);
        if (updatedUser.getId() == null) {
            log.error("User id is empty");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, new IllegalArgumentException("User id is empty"));
        }
        if (!users.containsKey(updatedUser.getId())) {
            log.warn("User with id {} does not exist", updatedUser.getId());
            throw new ErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    new IllegalArgumentException("User with id " + updatedUser.getId() + " does not exist")
            );
        }

        log.info("User updated user with id {}", updatedUser.getId());
        users.put(updatedUser.getId(), updatedUser);
        return updatedUser;
    }

}
