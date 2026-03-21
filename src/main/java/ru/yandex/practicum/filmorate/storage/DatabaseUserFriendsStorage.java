package ru.yandex.practicum.filmorate.storage;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Repository("databaseUserFriendsStorage")
public class DatabaseUserFriendsStorage extends DatabaseStorage<User> implements UserFriendsStorage {
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT DISTINCT u.id, u.email, u.login, u.name, u.birthday\n" +
            "FROM users u\n" +
            "JOIN user_friends uf1 ON u.id = uf1.friend_id AND uf1.user_id = ?\n" +
            "JOIN user_friends uf2 ON u.id = uf2.friend_id AND uf2.user_id = ?;";
    private static final String FIND_FRIENDS_BY_USER_ID_QUERY = "SELECT * FROM users where id in (SELECT friend_id FROM user_friends WHERE user_id = ?)";
    private static final String SAVE_USER_FRIEND_QUERY = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIENDS_BY_USER_ID_AND_FRIEND_ID_QUERY = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";

    public DatabaseUserFriendsStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
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
