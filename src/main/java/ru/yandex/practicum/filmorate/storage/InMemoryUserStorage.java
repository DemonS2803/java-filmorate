package ru.yandex.practicum.filmorate.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long userCurrentId = 1L;

    @Override
    public List<User> findAll() {
        return users.values().stream().toList();
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            log.info("User id is empty. Set new id: {}", userCurrentId);
            user.setId(userCurrentId++);
        } else if (users.containsKey(user.getId()) || user.getId() < userCurrentId) {
            log.warn("User with id {} already exists. Set new id: {}", user.getId(), userCurrentId);
            user.setId(userCurrentId++);
        }
        users.put(user.getId(), user);
        log.info("User added new user: {}", user);
        userCurrentId = Math.max(userCurrentId, user.getId() + 1);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new InvalidUserDataException("User id is empty. Failed to update user");
        }
        if (!users.containsKey(user.getId())) {
            throw new NoUserFoundException("User with id " + user.getId() + " not found");
        }

        log.info("User updated user with id {}", user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean delete(Long id) {
        return users.remove(id) != null;
    }
}
