package ru.yandex.practicum.filmorate.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<User> getUsers() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        return userStorage.save(user);
    }

    public User updateUser(User user) {
        List<User> users = userStorage.findAll();
        return userStorage.update(user);
    }

}
