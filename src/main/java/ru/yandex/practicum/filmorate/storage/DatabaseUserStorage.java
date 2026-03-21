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
    private static final String SAVE_USER_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE id = ?";

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

}
