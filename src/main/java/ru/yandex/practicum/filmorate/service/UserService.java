package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
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
        log.debug("Get user by id: {}", id);
        return userStorage.findUserByIdOrThrow(id);
    }

    public User createUser(User user) {
        log.info("Create user: {}", user);
        return userStorage.save(user);
    }

    public User updateUser(User user) {
        // check for user exists
        getUserById(user.getId());

        log.info("Update user: {}", user);
        return userStorage.update(user);
    }

    public Set<User> getUserFriends(long userId) {
        log.debug("Get user friends: {}", userId);
        return getUserById(userId).getFriends().stream()
                .map(this::getUserById)
                .filter(friend -> friend.getId() != userId)
                .collect(Collectors.toSet());
    }

    public User addToFriend(Long userId, Long friendId) {
        log.info("User {} make friends with {}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (Objects.equals(user.getId(), friend.getId())) {
            throw new InvalidUserDataException("User can't make friends with himself");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        user = userStorage.update(user);
        userStorage.update(friend);

        return user;
    }


    public User removeFromFriends(Long userId, Long friendId) {
        log.info("User {} remove {} from friends", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (Objects.equals(user.getId(), friend.getId())) {
            throw new InvalidUserDataException("User can't remove himself from friends");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        user = userStorage.update(user);
        userStorage.update(friend);

        return user;
    }

    public Set<User> getCommonFriends(Long userId, Long anotherUserId) {
        log.debug("Get common friends for {} and {}", userId, anotherUserId);
        User user = getUserById(userId);
        User another = getUserById(anotherUserId);

        if (Objects.equals(user.getId(), another.getId())) {
            throw new InvalidUserDataException("User can't get common friends with himself");
        }

        Set<Long> commonFriends = user.getFriends();
        commonFriends.retainAll(another.getFriends());

        return commonFriends.stream()
                .map(this::getUserById)
                .collect(Collectors.toSet());
    }

}
