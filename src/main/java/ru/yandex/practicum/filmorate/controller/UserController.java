package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;


@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<User> getUsers() {
        log.debug("Get all users info");
        return userService.getUsers();
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody UserDto incomingUserDto) {
        log.info("Add user: {} request", incomingUserDto);
        User user = userService.createUser(User.of(incomingUserDto));

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto incomingUserDto) {
        log.info("Update user {} request", incomingUserDto);
        User user = userService.updateUser(User.of(incomingUserDto));

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
