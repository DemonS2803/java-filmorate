package ru.yandex.practicum.filmorate.storage;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

@Primary
@Repository("databaseUserStorage")
public class DatabaseUserStorage extends DatabaseStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String SAVE_USER_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String FIND_FRIENDS_BY_USER_ID_QUERY = "SELECT friend_id FROM user_friends WHERE user_id = ?";
    private static final String SAVE_USER_FRIEND_QUERY = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_USER_FRIEND_QUERY = "DELETE FROM user_friends WHERE user_id = ?";

    @Autowired
    public DatabaseUserStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY).stream()
                .map(this::loadUserFriends)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return findOne(FIND_USER_BY_ID_QUERY, id)
                .map(this::loadUserFriends);
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
        return user;
    }

    @Override
    public User update(User user) {
        update(UPDATE_USER_QUERY,
            user.getEmail(),
            user.getLogin(),
            user.getName(),
            user.getBirthday() != null ? new Date(user.getBirthday().getTime()) : null,
            user.getId()
        );

        updateUserFriends(user.getId(), user.getFriends());

        return user;
    }

    @Override
    public boolean delete(Long id) {
        return delete(DELETE_USER_QUERY, id);
    }

    private User loadUserFriends(User user) {
        Set<Long> friends = new HashSet<>(jdbc.queryForList(FIND_FRIENDS_BY_USER_ID_QUERY, Long.class, user.getId()));
        user.setFriends(friends);
        return user;
    }

    private void saveUserFriends(Long userId, Set<Long> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return;
        }

        for (Long friendId : friendIds) {
            jdbc.update(SAVE_USER_FRIEND_QUERY, userId, friendId);
        }
    }

    private void updateUserFriends(Long userId, Set<Long> newFriendIds) {
        jdbc.update(DELETE_USER_FRIEND_QUERY, userId);

        if (newFriendIds != null && !newFriendIds.isEmpty()) {
            saveUserFriends(userId, newFriendIds);
        }
    }
}
