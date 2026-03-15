package ru.yandex.practicum.filmorate.controller;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.yandex.practicum.filmorate.dto.NewUserRequestDto;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequestDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserDataException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

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

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateUserRequestDto validUpdateUserRequestDto;
    private NewUserRequestDto validNewUserRequestDto;
    private User validUser;
    private UserDto validUserDto;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setUp() throws Exception {
        validUpdateUserRequestDto = new UpdateUserRequestDto();
        validUpdateUserRequestDto.setId(1L);
        validUpdateUserRequestDto.setEmail("user@example.com");
        validUpdateUserRequestDto.setLogin("login");
        validUpdateUserRequestDto.setName("username");
        validUpdateUserRequestDto.setBirthday(dateFormat.parse("1990-01-01"));

        validNewUserRequestDto = new NewUserRequestDto();
        validNewUserRequestDto.setEmail("user@example.com");
        validNewUserRequestDto.setLogin("login");
        validNewUserRequestDto.setName("username");
        validNewUserRequestDto.setBirthday(dateFormat.parse("1990-01-01"));

        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("user@example.com");
        validUser.setLogin("login");
        validUser.setName("username");
        validUser.setBirthday(dateFormat.parse("1990-01-01"));
        validUserDto = UserMapper.mapToUserDto(validUser);
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
        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(validUserDto);

        // Mock get users
        List<UserDto> usersList = Collections.singletonList(validUserDto);
        when(userService.getUsers()).thenReturn(usersList);

        // Test add user
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validNewUserRequestDto)))
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

        verify(userService, times(1)).createUser(any(NewUserRequestDto.class));
        verify(userService, times(1)).getUsers();
    }

    @Test
    void testUserController_addUser_WithoutId_ShouldGenerateNewId() throws Exception {
        validUpdateUserRequestDto.setId(null);

        User firstCreatedUser = new User();
        firstCreatedUser.setId(1L);
        firstCreatedUser.setEmail(validUpdateUserRequestDto.getEmail());
        firstCreatedUser.setLogin(validUpdateUserRequestDto.getLogin());
        firstCreatedUser.setName(validUpdateUserRequestDto.getName());
        firstCreatedUser.setBirthday(validUpdateUserRequestDto.getBirthday());
        UserDto firstUserDto = UserMapper.mapToUserDto(firstCreatedUser);

        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(firstUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        UpdateUserRequestDto secondUser = new UpdateUserRequestDto();
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
        UserDto secondUserDto = UserMapper.mapToUserDto(secondCreatedUser);

        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(secondUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));

        verify(userService, times(2)).createUser(any(NewUserRequestDto.class));
    }

    @Test
    void testUserController_addUser_WithExistingId_ShouldUseProvidedId() throws Exception {
        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(validUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        UpdateUserRequestDto duplicateUser = new UpdateUserRequestDto();
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

        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(UserMapper.mapToUserDto(duplicateCreatedUser));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1))) // ID remains 1 as provided
                .andExpect(jsonPath("$.email", is("duplicate@example.com")));

        verify(userService, times(2)).createUser(any(NewUserRequestDto.class));
    }

    @Test
    void testUserController_updateUser_ShouldUpdateExistingUser() throws Exception {
        // First create a user
        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(validUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isOk());

        // Prepare updated user
        UpdateUserRequestDto updatedUpdateUserRequestDto = new UpdateUserRequestDto();
        updatedUpdateUserRequestDto.setId(1L);
        updatedUpdateUserRequestDto.setEmail("updated@example.com");
        updatedUpdateUserRequestDto.setLogin("updatedLogin");
        updatedUpdateUserRequestDto.setName("Updated Name");
        updatedUpdateUserRequestDto.setBirthday(dateFormat.parse("1985-01-01"));

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(dateFormat.parse("1985-01-01"));
        UserDto updatedUserDto = UserMapper.mapToUserDto(updatedUser);

        // Mock update
        when(userService.updateUser(any(UpdateUserRequestDto.class))).thenReturn(updatedUserDto);

        // Mock get users after update
        List<UserDto> usersAfterUpdate = Collections.singletonList(updatedUserDto);
        when(userService.getUsers()).thenReturn(usersAfterUpdate);

        // Test update
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUpdateUserRequestDto)))
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

        verify(userService, times(1)).createUser(any(NewUserRequestDto.class));
        verify(userService, times(1)).updateUser(any(UpdateUserRequestDto.class));
        verify(userService, times(1)).getUsers();
    }

    @Test
    void testUserController_updateUser_WithoutId_ShouldReturnBadRequest() throws Exception {
        validUpdateUserRequestDto.setId(null);

        when(userService.updateUser(any(UpdateUserRequestDto.class)))
                .thenThrow(new InvalidUserDataException("User id is empty. Failed to update user"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(any(UpdateUserRequestDto.class));
    }

    @Test
    void testUserController_updateUser_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        validUpdateUserRequestDto.setId(999L);

        when(userService.updateUser(any(UpdateUserRequestDto.class)))
                .thenThrow(new NoUserFoundException("User not found"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(any(UpdateUserRequestDto.class));
    }

    @ParameterizedTest
    @MethodSource("invalidUserProvider")
    void testUserController_addUser_WithInvalidData_ShouldReturnBadRequest(UpdateUserRequestDto invalidUpdateUserRequestDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateUserRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(NewUserRequestDto.class));
    }

    @ParameterizedTest
    @MethodSource("invalidUserProvider")
    void testUserController_updateUser_WithInvalidData_ShouldReturnBadRequest(UpdateUserRequestDto invalidUpdateUserRequestDto) throws Exception {
        // Create existing user first
        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(validUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isOk());

        // Try to update with invalid data
        invalidUpdateUserRequestDto.setId(1L);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateUserRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any(UpdateUserRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "123@example.com"
    })
    void testUserController_addUser_WithValidEmails_ShouldSucceed(String email) throws Exception {
        validUpdateUserRequestDto.setEmail(email);

        User userWithEmail = new User();
        userWithEmail.setId(1L);
        userWithEmail.setEmail(email);
        userWithEmail.setLogin(validUpdateUserRequestDto.getLogin());
        userWithEmail.setName(validUpdateUserRequestDto.getName());
        userWithEmail.setBirthday(validUpdateUserRequestDto.getBirthday());

        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(UserMapper.mapToUserDto(userWithEmail));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)));

        verify(userService, times(1)).createUser(any(NewUserRequestDto.class));
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
        validUpdateUserRequestDto.setEmail(email);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(NewUserRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "user123", "john_doe"})
    void testUserController_addUser_WithValidLogins_ShouldSucceed(String login) throws Exception {
        validUpdateUserRequestDto.setLogin(login);

        User userWithLogin = new User();
        userWithLogin.setId(1L);
        userWithLogin.setEmail(validUpdateUserRequestDto.getEmail());
        userWithLogin.setLogin(login);
        userWithLogin.setName(validUpdateUserRequestDto.getName());
        userWithLogin.setBirthday(validUpdateUserRequestDto.getBirthday());

        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(UserMapper.mapToUserDto(userWithLogin));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login", is(login)));

        verify(userService, times(1)).createUser(any(NewUserRequestDto.class));
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
        validUpdateUserRequestDto.setLogin(login);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(NewUserRequestDto.class));
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
        validUpdateUserRequestDto.setBirthday(birthday);

        User userWithBirthday = new User();
        userWithBirthday.setId(1L);
        userWithBirthday.setEmail(validUpdateUserRequestDto.getEmail());
        userWithBirthday.setLogin(validUpdateUserRequestDto.getLogin());
        userWithBirthday.setName(validUpdateUserRequestDto.getName());
        userWithBirthday.setBirthday(birthday);

        when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(UserMapper.mapToUserDto(userWithBirthday));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthday", is(dateFormat.format(birthday))));

        verify(userService, times(1)).createUser(any(NewUserRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2126-01-01",
            "2125-12-31",
            "2030-06-15"
    })
    void testUserController_addUser_WithInvalidBirthdays_ShouldReject(String dateString) throws Exception {
        Date birthday = dateFormat.parse(dateString);
        validUpdateUserRequestDto.setBirthday(birthday);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(NewUserRequestDto.class));
    }

    @ParameterizedTest
    @MethodSource("emailValidationProvider")
    void testUserController_addUser_WithVariousEmails_ShouldValidateCorrectly(String email, boolean shouldBeValid) throws Exception {
        validUpdateUserRequestDto.setEmail(email);

        if (shouldBeValid) {
            User userWithEmail = new User();
            userWithEmail.setId(1L);
            userWithEmail.setEmail(email);
            userWithEmail.setLogin(validUpdateUserRequestDto.getLogin());
            userWithEmail.setName(validUpdateUserRequestDto.getName());
            userWithEmail.setBirthday(validUpdateUserRequestDto.getBirthday());

            when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(UserMapper.mapToUserDto(userWithEmail));
        }

        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)));

        if (shouldBeValid) {
            result.andExpect(status().isOk());
            verify(userService, times(1)).createUser(any(NewUserRequestDto.class));
        } else {
            result.andExpect(status().isBadRequest());
            verify(userService, never()).createUser(any(NewUserRequestDto.class));
        }
    }

    @ParameterizedTest
    @MethodSource("loginValidationProvider")
    void testUserController_addUser_WithVariousLogins_ShouldValidateCorrectly(String login, boolean shouldBeValid) throws Exception {
        validUpdateUserRequestDto.setLogin(login);

        if (shouldBeValid) {
            User userWithLogin = new User();
            userWithLogin.setId(1L);
            userWithLogin.setEmail(validUpdateUserRequestDto.getEmail());
            userWithLogin.setLogin(login);
            userWithLogin.setName(validUpdateUserRequestDto.getName());
            userWithLogin.setBirthday(validUpdateUserRequestDto.getBirthday());

            when(userService.createUser(any(NewUserRequestDto.class))).thenReturn(UserMapper.mapToUserDto(userWithLogin));
        }

        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateUserRequestDto)));

        if (shouldBeValid) {
            result.andExpect(status().isOk());
            verify(userService, times(1)).createUser(any(NewUserRequestDto.class));
        } else {
            result.andExpect(status().isBadRequest());
            verify(userService, never()).createUser(any(NewUserRequestDto.class));
        }
    }

    private static Stream<Arguments> invalidUserProvider() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        UpdateUserRequestDto nullEmailUser = new UpdateUserRequestDto();
        nullEmailUser.setId(2L);
        nullEmailUser.setEmail(null);
        nullEmailUser.setLogin("login");
        nullEmailUser.setName("Test User");
        nullEmailUser.setBirthday(dateFormat.parse("1990-01-01"));

        UpdateUserRequestDto emptyEmailUser = new UpdateUserRequestDto();
        emptyEmailUser.setId(3L);
        emptyEmailUser.setEmail("");
        emptyEmailUser.setLogin("login");
        emptyEmailUser.setName("Test User");
        emptyEmailUser.setBirthday(dateFormat.parse("1990-01-01"));

        UpdateUserRequestDto invalidEmailUser = new UpdateUserRequestDto();
        invalidEmailUser.setId(4L);
        invalidEmailUser.setEmail("invalid-email");
        invalidEmailUser.setLogin("login");
        invalidEmailUser.setName("Test User");
        invalidEmailUser.setBirthday(dateFormat.parse("1990-01-01"));

        UpdateUserRequestDto nullLoginUser = new UpdateUserRequestDto();
        nullLoginUser.setId(5L);
        nullLoginUser.setEmail("user@example.com");
        nullLoginUser.setLogin(null);
        nullLoginUser.setName("Test User");
        nullLoginUser.setBirthday(dateFormat.parse("1990-01-01"));

        UpdateUserRequestDto emptyLoginUser = new UpdateUserRequestDto();
        emptyLoginUser.setId(6L);
        emptyLoginUser.setEmail("user@example.com");
        emptyLoginUser.setLogin("");
        emptyLoginUser.setName("Test User");
        emptyLoginUser.setBirthday(dateFormat.parse("1990-01-01"));

        UpdateUserRequestDto whitespaceLoginUser = new UpdateUserRequestDto();
        whitespaceLoginUser.setId(7L);
        whitespaceLoginUser.setEmail("user@example.com");
        whitespaceLoginUser.setLogin("login with spaces");
        whitespaceLoginUser.setName("Test User");
        whitespaceLoginUser.setBirthday(dateFormat.parse("1990-01-01"));

        UpdateUserRequestDto futureBirthdayUser = new UpdateUserRequestDto();
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
