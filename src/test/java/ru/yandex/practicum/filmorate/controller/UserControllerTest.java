package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({UserController.class, ExceptionHandlerController.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

//    @MockBean
//    private UserController userController;
    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto validUserDto;
    private User validUser;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setUp() throws Exception {
//        mockMvc =
//                MockMvcBuilders.standaloneSetup(backupController)
//                        .setControllerAdvice(new ExceptionHandlerController(localizationService))
//                        .build();
        validUserDto = new UserDto();
        validUserDto.setId(1L);
        validUserDto.setEmail("user@example.com");
        validUserDto.setLogin("login");
        validUserDto.setName("username");
        validUserDto.setBirthday(dateFormat.parse("1990-01-01"));

        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("user@example.com");
        validUser.setLogin("login");
        validUser.setName("username");
        validUser.setBirthday(dateFormat.parse("1990-01-01"));
    }

    @Test
    void testUserController_getUsers_ShouldReturnEmptyList() throws Exception {
        when(userService.getUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).getUsers();
    }

    @Test
    void testUserController_addUser_And_GetUsers_ShouldWorkTogether() throws Exception {
        // Mock create user
        when(userService.createUser(any(User.class))).thenReturn(validUser);

        // Mock get users
        List<User> usersList = Collections.singletonList(validUser);
        when(userService.getUsers()).thenReturn(usersList);

        // Test add user
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.login", is("login")))
                .andExpect(jsonPath("$.name", is("username")));

        // Test get users
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].email", is("user@example.com")))
                .andExpect(jsonPath("$[0].login", is("login")));

        verify(userService, times(1)).createUser(any(User.class));
        verify(userService, times(1)).getUsers();
    }

    @Test
    void testUserController_addUser_WithoutId_ShouldGenerateNewId() throws Exception {
        validUserDto.setId(null);

        User firstCreatedUser = new User();
        firstCreatedUser.setId(1L);
        firstCreatedUser.setEmail(validUserDto.getEmail());
        firstCreatedUser.setLogin(validUserDto.getLogin());
        firstCreatedUser.setName(validUserDto.getName());
        firstCreatedUser.setBirthday(validUserDto.getBirthday());

        when(userService.createUser(any(User.class))).thenReturn(firstCreatedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        UserDto secondUser = new UserDto();
        secondUser.setEmail("second@example.com");
        secondUser.setLogin("secondLogin");
        secondUser.setName("Second User");
        secondUser.setBirthday(dateFormat.parse("1995-01-01"));

        User secondCreatedUser = new User();
        secondCreatedUser.setId(2L);
        secondCreatedUser.setEmail("second@example.com");
        secondCreatedUser.setLogin("secondLogin");
        secondCreatedUser.setName("Second User");
        secondCreatedUser.setBirthday(dateFormat.parse("1995-01-01"));

        when(userService.createUser(any(User.class))).thenReturn(secondCreatedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));

        verify(userService, times(2)).createUser(any(User.class));
    }

    @Test
    void testUserController_addUser_WithExistingId_ShouldUseProvidedId() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(validUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        UserDto duplicateUser = new UserDto();
        duplicateUser.setId(1L);
        duplicateUser.setEmail("duplicate@example.com");
        duplicateUser.setLogin("duplicateLogin");
        duplicateUser.setName("Duplicate User");
        duplicateUser.setBirthday(dateFormat.parse("2000-01-01"));

        User duplicateCreatedUser = new User();
        duplicateCreatedUser.setId(1L); // Service returns with ID 1 as provided
        duplicateCreatedUser.setEmail("duplicate@example.com");
        duplicateCreatedUser.setLogin("duplicateLogin");
        duplicateCreatedUser.setName("Duplicate User");
        duplicateCreatedUser.setBirthday(dateFormat.parse("2000-01-01"));

        when(userService.createUser(any(User.class))).thenReturn(duplicateCreatedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1))) // ID remains 1 as provided
                .andExpect(jsonPath("$.email", is("duplicate@example.com")));

        verify(userService, times(2)).createUser(any(User.class));
    }

    @Test
    void testUserController_updateUser_ShouldUpdateExistingUser() throws Exception {
        // First create a user
        when(userService.createUser(any(User.class))).thenReturn(validUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());

        // Prepare updated user
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setId(1L);
        updatedUserDto.setEmail("updated@example.com");
        updatedUserDto.setLogin("updatedLogin");
        updatedUserDto.setName("Updated Name");
        updatedUserDto.setBirthday(dateFormat.parse("1985-01-01"));

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(dateFormat.parse("1985-01-01"));

        // Mock update
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        // Mock get users after update
        List<User> usersAfterUpdate = Collections.singletonList(updatedUser);
        when(userService.getUsers()).thenReturn(usersAfterUpdate);

        // Test update
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.login", is("updatedLogin")))
                .andExpect(jsonPath("$.name", is("Updated Name")));

        // Verify update worked via get
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", is("updated@example.com")))
                .andExpect(jsonPath("$[0].login", is("updatedLogin")));

        verify(userService, times(1)).createUser(any(User.class));
        verify(userService, times(1)).updateUser(any(User.class));
        verify(userService, times(1)).getUsers();
    }

    @Test
    void testUserController_updateUser_WithoutId_ShouldReturnBadRequest() throws Exception {
        validUserDto.setId(null);

        when(userService.updateUser(any(User.class)))
                .thenThrow(new InvalidUserDataException("User id is empty. Failed to update user"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void testUserController_updateUser_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        validUserDto.setId(999L);

        when(userService.updateUser(any(User.class)))
                .thenThrow(new NoUserFoundException("User not found"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("invalidUserProvider")
    void testUserController_addUser_WithInvalidData_ShouldReturnBadRequest(UserDto invalidUserDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("invalidUserProvider")
    void testUserController_updateUser_WithInvalidData_ShouldReturnBadRequest(UserDto invalidUserDto) throws Exception {
        // Create existing user first
        when(userService.createUser(any(User.class))).thenReturn(validUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());

        // Try to update with invalid data
        invalidUserDto.setId(1L);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "123@example.com"
    })
    void testUserController_addUser_WithValidEmails_ShouldSucceed(String email) throws Exception {
        validUserDto.setEmail(email);

        User userWithEmail = new User();
        userWithEmail.setId(1L);
        userWithEmail.setEmail(email);
        userWithEmail.setLogin(validUserDto.getLogin());
        userWithEmail.setName(validUserDto.getName());
        userWithEmail.setBirthday(validUserDto.getBirthday());

        when(userService.createUser(any(User.class))).thenReturn(userWithEmail);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email",
            "invalid@",
            "@domain.com",
            "user@.com",
            "user@domain.",
            "user name@domain.com"
    })
    void testUserController_addUser_WithInvalidEmails_ShouldReject(String email) throws Exception {
        validUserDto.setEmail(email);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "user123", "john_doe"})
    void testUserController_addUser_WithValidLogins_ShouldSucceed(String login) throws Exception {
        validUserDto.setLogin(login);

        User userWithLogin = new User();
        userWithLogin.setId(1L);
        userWithLogin.setEmail(validUserDto.getEmail());
        userWithLogin.setLogin(login);
        userWithLogin.setName(validUserDto.getName());
        userWithLogin.setBirthday(validUserDto.getBirthday());

        when(userService.createUser(any(User.class))).thenReturn(userWithLogin);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login", is(login)));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "login with spaces",
            "login\twith\ttabs",
            "login\nwith\nnewlines",
            " login",
            "login ",
            "  login  "
    })
    void testUserController_addUser_WithInvalidLogins_ShouldReject(String login) throws Exception {
        validUserDto.setLogin(login);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1990-01-01",
            "2000-12-31",
            "2024-01-01",
            "2024-02-14"
    })
    void testUserController_addUser_WithValidBirthdays_ShouldSucceed(String dateString) throws Exception {
        Date birthday = new Date(dateFormat.parse(dateString).getTime() + 30000000);
        validUserDto.setBirthday(birthday);

        User userWithBirthday = new User();
        userWithBirthday.setId(1L);
        userWithBirthday.setEmail(validUserDto.getEmail());
        userWithBirthday.setLogin(validUserDto.getLogin());
        userWithBirthday.setName(validUserDto.getName());
        userWithBirthday.setBirthday(birthday);

        when(userService.createUser(any(User.class))).thenReturn(userWithBirthday);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthday", is(dateFormat.format(birthday))));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2126-01-01",
            "2125-12-31",
            "2030-06-15"
    })
    void testUserController_addUser_WithInvalidBirthdays_ShouldReject(String dateString) throws Exception {
        Date birthday = dateFormat.parse(dateString);
        validUserDto.setBirthday(birthday);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("emailValidationProvider")
    void testUserController_addUser_WithVariousEmails_ShouldValidateCorrectly(String email, boolean shouldBeValid) throws Exception {
        validUserDto.setEmail(email);

        if (shouldBeValid) {
            User userWithEmail = new User();
            userWithEmail.setId(1L);
            userWithEmail.setEmail(email);
            userWithEmail.setLogin(validUserDto.getLogin());
            userWithEmail.setName(validUserDto.getName());
            userWithEmail.setBirthday(validUserDto.getBirthday());

            when(userService.createUser(any(User.class))).thenReturn(userWithEmail);
        }

        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDto)));

        if (shouldBeValid) {
            result.andExpect(status().isOk());
            verify(userService, times(1)).createUser(any(User.class));
        } else {
            result.andExpect(status().isBadRequest());
            verify(userService, never()).createUser(any(User.class));
        }
    }

    @ParameterizedTest
    @MethodSource("loginValidationProvider")
    void testUserController_addUser_WithVariousLogins_ShouldValidateCorrectly(String login, boolean shouldBeValid) throws Exception {
        validUserDto.setLogin(login);

        if (shouldBeValid) {
            User userWithLogin = new User();
            userWithLogin.setId(1L);
            userWithLogin.setEmail(validUserDto.getEmail());
            userWithLogin.setLogin(login);
            userWithLogin.setName(validUserDto.getName());
            userWithLogin.setBirthday(validUserDto.getBirthday());

            when(userService.createUser(any(User.class))).thenReturn(userWithLogin);
        }

        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDto)));

        if (shouldBeValid) {
            result.andExpect(status().isOk());
            verify(userService, times(1)).createUser(any(User.class));
        } else {
            result.andExpect(status().isBadRequest());
            verify(userService, never()).createUser(any(User.class));
        }
    }

    private static Stream<Arguments> invalidUserProvider() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        UserDto nullEmailUser = new UserDto();
        nullEmailUser.setId(2L);
        nullEmailUser.setEmail(null);
        nullEmailUser.setLogin("login");
        nullEmailUser.setName("Test User");
        nullEmailUser.setBirthday(dateFormat.parse("1990-01-01"));

        UserDto emptyEmailUser = new UserDto();
        emptyEmailUser.setId(3L);
        emptyEmailUser.setEmail("");
        emptyEmailUser.setLogin("login");
        emptyEmailUser.setName("Test User");
        emptyEmailUser.setBirthday(dateFormat.parse("1990-01-01"));

        UserDto invalidEmailUser = new UserDto();
        invalidEmailUser.setId(4L);
        invalidEmailUser.setEmail("invalid-email");
        invalidEmailUser.setLogin("login");
        invalidEmailUser.setName("Test User");
        invalidEmailUser.setBirthday(dateFormat.parse("1990-01-01"));

        UserDto nullLoginUser = new UserDto();
        nullLoginUser.setId(5L);
        nullLoginUser.setEmail("user@example.com");
        nullLoginUser.setLogin(null);
        nullLoginUser.setName("Test User");
        nullLoginUser.setBirthday(dateFormat.parse("1990-01-01"));

        UserDto emptyLoginUser = new UserDto();
        emptyLoginUser.setId(6L);
        emptyLoginUser.setEmail("user@example.com");
        emptyLoginUser.setLogin("");
        emptyLoginUser.setName("Test User");
        emptyLoginUser.setBirthday(dateFormat.parse("1990-01-01"));

        UserDto whitespaceLoginUser = new UserDto();
        whitespaceLoginUser.setId(7L);
        whitespaceLoginUser.setEmail("user@example.com");
        whitespaceLoginUser.setLogin("login with spaces");
        whitespaceLoginUser.setName("Test User");
        whitespaceLoginUser.setBirthday(dateFormat.parse("1990-01-01"));

        UserDto futureBirthdayUser = new UserDto();
        futureBirthdayUser.setId(8L);
        futureBirthdayUser.setEmail("user@example.com");
        futureBirthdayUser.setLogin("login");
        futureBirthdayUser.setName("Test User");
        futureBirthdayUser.setBirthday(dateFormat.parse("2126-01-01"));

        return Stream.of(
                Arguments.of(nullEmailUser),
                Arguments.of(emptyEmailUser),
                Arguments.of(invalidEmailUser),
                Arguments.of(nullLoginUser),
                Arguments.of(emptyLoginUser),
                Arguments.of(whitespaceLoginUser),
                Arguments.of(futureBirthdayUser)
        );
    }

    private static Stream<Arguments> emailValidationProvider() {
        return Stream.of(
                Arguments.of("user@example.com", true),
                Arguments.of("user.name@example.com", true),
                Arguments.of("user+tag@example.co.uk", true),
                Arguments.of("123@example.com", true),
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of("   ", false),
                Arguments.of("invalid-email", false),
                Arguments.of("invalid@", false),
                Arguments.of("@domain.com", false)
        );
    }

    private static Stream<Arguments> loginValidationProvider() {
        return Stream.of(
                Arguments.of("login", true),
                Arguments.of("user123", true),
                Arguments.of("john_doe", true),
                Arguments.of("login", true),
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of("   ", false),
                Arguments.of("login with spaces", false),
                Arguments.of("login\twith\ttabs", false),
                Arguments.of(" login", false),
                Arguments.of("login ", false)
        );
    }
}
