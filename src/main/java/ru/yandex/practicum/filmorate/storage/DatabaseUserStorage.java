package ru.yandex.practicum.filmorate.storage;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Primary
@Repository("databaseUserStorage")
public class DatabaseUserStorage extends DatabaseStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT DISTINCT u.id, u.email, u.login, u.name, u.birthday\n" +
            "FROM users u\n" +
            "JOIN user_friends uf1 ON u.id = uf1.friend_id AND uf1.user_id = ?\n" +
            "JOIN user_friends uf2 ON u.id = uf2.friend_id AND uf2.user_id = ?;";
    private static final String SAVE_USER_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String FIND_FRIENDS_BY_USER_ID_QUERY = "SELECT * FROM users where id in (SELECT friend_id FROM user_friends WHERE user_id = ?)";
    private static final String SAVE_USER_FRIEND_QUERY = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIENDS_BY_USER_ID_AND_FRIEND_ID_QUERY = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";

    @Autowired
    public DatabaseUserStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        log.debug("Find user by id {}", id);
        return findOne(FIND_USER_BY_ID_QUERY, id);
    }

    @Override
    public User save(User user) {
        long id = insert(SAVE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        log.debug("Saved new user with id {}", id);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new InvalidUserDataException("User id is null. Failed to update user");
        }

        update(UPDATE_USER_QUERY,
            user.getEmail(),
            user.getLogin(),
            user.getName(),
            user.getBirthday() != null ? new Date(user.getBirthday().getTime()) : null,
            user.getId()
        );

        log.debug("Updated user with id {}", user.getId());
        return user;
    }

    @Override
    public boolean delete(Long id) {
        log.info("Delete user with id {}", id);
        return delete(DELETE_USER_QUERY, id);
    }

    @Override
    public List<User> findUserFriends(Long id) {
        log.debug("Find user friends by id {}", id);
        return findMany(FIND_FRIENDS_BY_USER_ID_QUERY, id);
    }

    @Override
    public boolean addFriend(Long userId, Long friendId) {
        log.debug("Add friend {} for {}", friendId, userId);
        if (friendId == null || userId == null) {
            return false;
        }

        jdbc.update(SAVE_USER_FRIEND_QUERY, userId, friendId);
        return true;
    }

    @Override
    public boolean removeFriend(Long userId, Long friendId) {
        if (friendId == null || userId == null) {
            return false;
        }

        jdbc.update(DELETE_FRIENDS_BY_USER_ID_AND_FRIEND_ID_QUERY, userId, friendId);
        return true;
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long friendId) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId, friendId);
    }

}
