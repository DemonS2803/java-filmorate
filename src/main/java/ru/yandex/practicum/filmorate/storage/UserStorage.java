package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {

    List<User> findAll();

    Optional<User> findUserById(Long id);

    User save(User user);

    User update(User user);

    boolean delete(Long id);

    List<User> findUserFriends(Long id);

    boolean addFriend(Long userId, Long friendId);

    boolean removeFriend(Long userId, Long friendId);

    List<User> findCommonFriends(Long userId, Long friendId);

}
