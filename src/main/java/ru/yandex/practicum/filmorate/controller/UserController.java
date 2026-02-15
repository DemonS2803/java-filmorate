package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.ErrorResponse;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private Long userCurrentId = 1L;

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody UserDto incomingUserDto) {
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
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto incomingUserDto) {
        log.info("User want update user: {}", incomingUserDto);
        User updatedUser = User.of(incomingUserDto);
        if (updatedUser.getId() == null) {
            log.error("User id is empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!users.containsKey(updatedUser.getId())) {
            String error = "User with id " + updatedUser.getId() + " not found";
            log.warn(error);
            return new ResponseEntity<>(
                    new ErrorResponse(404, "Failed to create user", error),
                    HttpStatus.NOT_FOUND
            );
        }

        log.info("User updated user with id {}", updatedUser.getId());
        users.put(updatedUser.getId(), updatedUser);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

}
