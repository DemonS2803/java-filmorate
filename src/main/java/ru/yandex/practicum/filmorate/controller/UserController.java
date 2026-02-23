package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import java.util.Set;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> makeFriendsWith(
            @PathVariable("id") Long id,
            @PathVariable("friendId") Long friendId
    ) {
        log.info("User {} want make friends with user {} ", id, friendId);
        User user = userService.addToFriend(id, friendId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> deleteFromFriends(
            @PathVariable("id") Long id,
            @PathVariable("friendId") Long friendId
    ) {
        log.info("User {} want delete user {} from friends", id, friendId);
        User user = userService.removeFromFriends(id, friendId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Collection<User>> getUserFriends(@PathVariable Long id) {
        log.debug("Get user {} friends list", id);
        return new ResponseEntity<>(userService.getUserFriends(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> getUserFriends(
            @PathVariable("id") Long id,
            @PathVariable("otherId") Long otherId
    ) {
        log.debug("Get common friends list for users {} and {}", id, otherId);
        Set<User> commonFriends = userService.getCommonFriends(id, otherId);
        return new ResponseEntity<>(commonFriends, HttpStatus.OK);
    }

}
