package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;
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

    public User getUserById(long id) {
        return userStorage.findUserById(id)
            .orElseThrow(() -> new NoUserFoundException("User with id " + id + " not found"));
    }

    public User createUser(User user) {
        return userStorage.save(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public Set<User> getUserFriends(long userId) {
        return getUserById(userId).getFriends().stream()
                .map(this::getUserById)
                .filter(friend -> friend.getId() != userId)
                .collect(Collectors.toSet());
    }

    public User addToFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        user = userStorage.update(user);
        userStorage.update(friend);

        return user;
    }


    public User removeFromFriends(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        user = userStorage.update(user);
        userStorage.update(friend);

        return user;
    }

    public Set<User> getCommonFriends(Long userId, Long anotherUserId) {
        User user = getUserById(userId);
        User another = getUserById(anotherUserId);

        Set<Long> commonFriends = user.getFriends();
        commonFriends.retainAll(another.getFriends());

        return commonFriends.stream()
                .map(this::getUserById)
                .collect(Collectors.toSet());
    }

}
