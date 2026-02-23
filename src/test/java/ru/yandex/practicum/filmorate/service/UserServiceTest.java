package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private User user3;
    private Date birthday;

    @BeforeEach
    void setUp() {
        LocalDate localDate = LocalDate.of(1990, 1, 1);
        birthday = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(birthday);
        user1.setFriends(new HashSet<>());

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(birthday);
        user2.setFriends(new HashSet<>());

        user3 = new User();
        user3.setId(3L);
        user3.setEmail("user3@test.com");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(birthday);
        user3.setFriends(new HashSet<>());
    }

    @Test
    void addToFriend_ShouldAddFriendship_WhenBothUsersExist() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.addToFriend(1L, 2L);

        assertTrue(user1.getFriends().contains(2L));
        assertTrue(user2.getFriends().contains(1L));
        assertEquals(1, user1.getFriends().size());
        assertEquals(1, user2.getFriends().size());

        verify(userStorage, times(2)).update(any(User.class));
        verify(userStorage).findUserById(1L);
        verify(userStorage).findUserById(2L);
    }

    @Test
    void addToFriend_ShouldThrowException_WhenUserNotFound() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.empty());

        assertThrows(NoUserFoundException.class, () ->
                userService.addToFriend(1L, 2L)
        );

        verify(userStorage).findUserById(1L);
        verify(userStorage, never()).findUserById(2L);
        verify(userStorage, never()).save(any());
    }

    @Test
    void addToFriend_ShouldThrowException_WhenFriendNotFound() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.empty());

        assertThrows(NoUserFoundException.class, () ->
                userService.addToFriend(1L, 2L)
        );

        verify(userStorage).findUserById(1L);
        verify(userStorage).findUserById(2L);
        verify(userStorage, never()).save(any());
    }

    @Test
    void addToFriend_ShouldNotCreateDuplicateFriendship_WhenAlreadyFriends() {
        user1.getFriends().add(2L);
        user2.getFriends().add(1L);

        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.addToFriend(1L, 2L);

        assertTrue(user1.getFriends().contains(2L));
        assertTrue(user2.getFriends().contains(1L));
        assertEquals(1, user1.getFriends().size());
        assertEquals(1, user2.getFriends().size());

        verify(userStorage, times(2)).update(any(User.class));
    }

    @Test
    void removeFromFriends_ShouldRemoveFriendship_WhenBothUsersExist() {
        user1.getFriends().add(2L);
        user2.getFriends().add(1L);

        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.removeFromFriends(1L, 2L);

        assertFalse(user1.getFriends().contains(2L));
        assertFalse(user2.getFriends().contains(1L));
        assertEquals(0, user1.getFriends().size());
        assertEquals(0, user2.getFriends().size());

        verify(userStorage, times(2)).update(any(User.class));
        verify(userStorage).findUserById(1L);
        verify(userStorage).findUserById(2L);
    }

    @Test
    void removeFromFriends_ShouldDoNothing_WhenUsersAreNotFriends() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.removeFromFriends(1L, 2L);

        assertFalse(user1.getFriends().contains(2L));
        assertFalse(user2.getFriends().contains(1L));
        assertEquals(0, user1.getFriends().size());
        assertEquals(0, user2.getFriends().size());

        verify(userStorage, times(2)).update(any(User.class));
    }

    @Test
    void removeFromFriends_ShouldThrowException_WhenUserNotFound() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.empty());

        assertThrows(NoUserFoundException.class, () ->
                userService.removeFromFriends(1L, 2L)
        );

        verify(userStorage).findUserById(1L);
        verify(userStorage, never()).findUserById(2L);
        verify(userStorage, never()).save(any());
    }

    @Test
    void getUserFriends_ShouldReturnSetOfFriends_WhenUserExists() {
        user1.getFriends().add(2L);
        user1.getFriends().add(3L);

        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.findUserById(3L)).thenReturn(Optional.of(user3));

        Set<User> friends = userService.getUserFriends(1L);

        assertEquals(2, friends.size());
        assertTrue(friends.contains(user2));
        assertTrue(friends.contains(user3));

        verify(userStorage).findUserById(1L);
        verify(userStorage).findUserById(2L);
        verify(userStorage).findUserById(3L);
    }

    @Test
    void getUserFriends_ShouldReturnEmptySet_WhenUserHasNoFriends() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));

        Set<User> friends = userService.getUserFriends(1L);

        assertTrue(friends.isEmpty());

        verify(userStorage).findUserById(1L);
    }

    @Test
    void getUserFriends_ShouldThrowException_WhenUserNotFound() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.empty());

        assertThrows(NoUserFoundException.class, () ->
                userService.getUserFriends(1L)
        );

        verify(userStorage).findUserById(1L);
    }

    @Test
    void getCommonFriends_ShouldReturnCommonFriends_WhenUsersExist() {
        user1.getFriends().add(2L);
        user1.getFriends().add(3L);
        user2.getFriends().add(1L);
        user2.getFriends().add(3L);

        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.findUserById(3L)).thenReturn(Optional.of(user3));

        Set<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(user3));

        verify(userStorage).findUserById(1L);
        verify(userStorage).findUserById(2L);
        verify(userStorage).findUserById(3L);
    }

    @Test
    void getCommonFriends_ShouldReturnEmptySet_WhenNoCommonFriends() {
        user1.getFriends().add(2L);
        user2.getFriends().add(3L);

        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));

        Set<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertTrue(commonFriends.isEmpty());

        verify(userStorage).findUserById(1L);
        verify(userStorage).findUserById(2L);
        verify(userStorage, never()).findUserById(3L);
    }

    @Test
    void getCommonFriends_ShouldReturnEmptySet_WhenOneUserHasNoFriends() {
        user1.getFriends().add(2L);

        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));

        Set<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertTrue(commonFriends.isEmpty());

        verify(userStorage).findUserById(1L);
        verify(userStorage).findUserById(2L);
    }

    @Test
    void getCommonFriends_ShouldThrowException_WhenFirstUserNotFound() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.empty());

        assertThrows(NoUserFoundException.class, () ->
                userService.getCommonFriends(1L, 2L)
        );

        verify(userStorage).findUserById(1L);
        verify(userStorage, never()).findUserById(2L);
    }

    @Test
    void getCommonFriends_ShouldThrowException_WhenSecondUserNotFound() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.empty());

        assertThrows(NoUserFoundException.class, () ->
                userService.getCommonFriends(1L, 2L)
        );

        verify(userStorage).findUserById(1L);
        verify(userStorage).findUserById(2L);
    }

    @Test
    void addToFriend_ShouldReturnUpdatedUser_WhenFriendshipAdded() {
        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.addToFriend(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getFriends().contains(2L));
    }

    @Test
    void removeFromFriends_ShouldReturnUpdatedUser_WhenFriendshipRemoved() {
        user1.getFriends().add(2L);
        user2.getFriends().add(1L);

        when(userStorage.findUserById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findUserById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.removeFromFriends(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertFalse(result.getFriends().contains(2L));
    }
}
