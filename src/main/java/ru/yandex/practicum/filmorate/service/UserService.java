package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.NewUserRequestDto;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequestDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserFriendsStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserFriendsStorage userFriendsStorage;

    public List<UserDto> getUsers() {
        return userStorage.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto getUserById(long id) {
        return UserMapper.mapToUserDto(getUserByIdOrThrow(id));
    }

    protected User getUserByIdOrThrow(Long id) {
        log.debug("Get user by id: {}", id);
        return userStorage.findUserById(id)
                .orElseThrow(() -> new NoUserFoundException("No user with id " + id + " found"));
    }

    public UserDto createUser(NewUserRequestDto createDto) {
        User user = UserMapper.mapToUser(createDto);
        log.info("Create user: {}", user);
        return UserMapper.mapToUserDto(userStorage.save(user));
    }

    public UserDto updateUser(UpdateUserRequestDto updateDto) {
        User user = UserMapper.mapToUser(updateDto);
        // check for user exists
        getUserById(user.getId());

        log.info("Update user: {}", user);
        return UserMapper.mapToUserDto(userStorage.update(user));
    }

    public Set<UserDto> getUserFriends(long userId) {
        getUserByIdOrThrow(userId);

        log.debug("Get user friends: {}", userId);
        return userFriendsStorage.findUserFriends(userId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toSet());
    }

    public UserDto addToFriend(Long userId, Long friendId) {
        log.info("User {} make friends with {}", userId, friendId);
        User user = getUserByIdOrThrow(userId);
        User friend = getUserByIdOrThrow(friendId);

        checkNotEquals(user, friend, "User can't make friends with himself");

        boolean isCreated = userFriendsStorage.addFriend(userId, friendId);
        if (!isCreated) {
            log.error("Can't add friend {} to user {}", friendId, userId);
        }
        user = getUserByIdOrThrow(userId);

        return UserMapper.mapToUserDto(user);
    }


    public UserDto removeFromFriends(Long userId, Long friendId) {
        log.info("User {} remove {} from friends", userId, friendId);
        User user = getUserByIdOrThrow(userId);
        User friend = getUserByIdOrThrow(friendId);

        checkNotEquals(user, friend, "User can't remove himself from friends");

        boolean isDeleted = userFriendsStorage.removeFriend(userId, friendId);
        if (!isDeleted) {
            log.error("Can't remove friend {} from user {}", friendId, userId);
        }
        user = getUserByIdOrThrow(userId);

        return UserMapper.mapToUserDto(user);
    }

    public Set<UserDto> getCommonFriends(Long userId, Long anotherUserId) {
        log.debug("Get common friends for {} and {}", userId, anotherUserId);
        User user = getUserByIdOrThrow(userId);
        User another = getUserByIdOrThrow(anotherUserId);

        checkNotEquals(user, another, "User can't get common friends with himself");

        return userFriendsStorage.findCommonFriends(userId, anotherUserId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toSet());
    }

    private void checkNotEquals(User user, User friend, String message) {
        if (Objects.equals(user.getId(), friend.getId())) {
            throw new InvalidUserDataException(message);
        }
        // if more comparing reasons will be in future (why not)
    }

}
