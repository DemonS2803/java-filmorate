package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserFriendsStorage {

    boolean addFriend(Long userId, Long friendId);

    boolean removeFriend(Long userId, Long friendId);

    List<User> findUserFriends(Long id);

    List<User> findCommonFriends(Long userId, Long friendId);

}
