package ru.yandex.practicum.filmorate.storage;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DatabaseUserStorageTest {

    @Autowired
    private final DatabaseUserStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(new Date(90, 0, 1)); // 1990-01-01
        testUser.setFriends(new HashSet<>());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userStorage.save(testUser);

        List<User> users = userStorage.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findUserById_WithExistingId_ShouldReturnUser() {
        User savedUser = userStorage.save(testUser);

        Optional<User> foundUser = userStorage.findUserById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
        assertEquals(savedUser.getLogin(), foundUser.get().getLogin());
    }

    @Test
    void findUserById_WithNonExistingId_ShouldReturnEmpty() {
        Optional<User> foundUser = userStorage.findUserById(999L);

        assertFalse(foundUser.isPresent());
    }

    @Test
    void save_ShouldCreateNewUser() {
        User savedUser = userStorage.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        assertEquals(testUser.getLogin(), savedUser.getLogin());
    }

    @Test
    void save_WithFriends_ShouldSaveFriends() {
        Set<Long> friends = new HashSet<>();
        friends.add(1L);
        friends.add(2L);
        testUser.setFriends(friends);

        User savedUser = userStorage.save(testUser);

        assertNotNull(savedUser.getId());
        assertThat(savedUser.getFriends()).hasSize(2);
    }

    @Test
    void update_ShouldModifyExistingUser() {
        User savedUser = userStorage.save(testUser);
        savedUser.setEmail("updated@example.com");
        savedUser.setName("Updated Name");

        User updatedUser = userStorage.update(savedUser);

        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("Updated Name", updatedUser.getName());

        Optional<User> foundUser = userStorage.findUserById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("updated@example.com", foundUser.get().getEmail());
    }

    @Test
    void update_WithFriends_ShouldUpdateFriends() {
        User savedUser = userStorage.save(testUser);

        Set<Long> friends = new HashSet<>();
        friends.add(1L);
        friends.add(2L);
        savedUser.setFriends(friends);

        User updatedUser = userStorage.update(savedUser);

        assertThat(updatedUser.getFriends()).hasSize(2);
    }

    @Test
    void delete_WithExistingId_ShouldReturnTrue() {
        User savedUser = userStorage.save(testUser);

        boolean deleted = userStorage.delete(savedUser.getId());

        assertTrue(deleted);
        Optional<User> foundUser = userStorage.findUserById(savedUser.getId());
        assertFalse(foundUser.isPresent());
    }

    @Test
    void delete_WithNonExistingId_ShouldReturnFalse() {
        boolean deleted = userStorage.delete(999L);

        assertFalse(deleted);
    }
}
