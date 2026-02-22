package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.UserDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto validUserDto;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setUp() throws Exception {
        validUserDto = new UserDto();
        validUserDto.setId(1L);
        validUserDto.setEmail("user@example.com");
        validUserDto.setLogin("login");
        validUserDto.setName("username");
        validUserDto.setBirthday(dateFormat.parse("1990-01-01"));

        ReflectionTestUtils.setField(userController, "users", new HashMap<>());
        ReflectionTestUtils.setField(userController, "userCurrentId", 1L);
    }

    @Test
    void testUserController_getUsers_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testUserController_addUser_And_GetUsers_ShouldWorkTogether() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.login", is("login")))
                .andExpect(jsonPath("$.name", is("username")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].email", is("user@example.com")))
                .andExpect(jsonPath("$[0].login", is("login")));
    }

    @Test
    void testUserController_addUser_WithoutId_ShouldGenerateNewId() throws Exception {
        validUserDto.setId(null);

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

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    void testUserController_addUser_WithExistingId_ShouldGenerateNewId() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDto)));

        UserDto duplicateUser = new UserDto();
        duplicateUser.setId(1L);
        duplicateUser.setEmail("duplicate@example.com");
        duplicateUser.setLogin("duplicateLogin");
        duplicateUser.setName("Duplicate User");
        duplicateUser.setBirthday(dateFormat.parse("2000-01-01"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.email", is("duplicate@example.com")));
    }

    @Test
    void testUserController_updateUser_ShouldUpdateExistingUser() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDto)));

        validUserDto.setEmail("updated@example.com");
        validUserDto.setLogin("updatedLogin");
        validUserDto.setName("Updated Name");
        validUserDto.setBirthday(dateFormat.parse("1985-01-01"));
        validUserDto.setBirthday(new Date(validUserDto.getBirthday().getTime() + 30000000));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.login", is("updatedLogin")))
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.birthday", is("1985-01-01")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", is("updated@example.com")))
                .andExpect(jsonPath("$[0].login", is("updatedLogin")));
    }

    @Test
    void testUserController_updateUser_WithoutId_ShouldReturnBadRequest() throws Exception {
        validUserDto.setId(null);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUserController_updateUser_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        validUserDto.setId(999L);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("invalidUserProvider")
    void testUserController_addUser_WithInvalidData_ShouldReturnBadRequest(UserDto invalidUserDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("invalidUserProvider")
    void testUserController_updateUser_WithInvalidData_ShouldReturnBadRequest(UserDto invalidUserDto) throws Exception {
        UserDto existingUser = new UserDto();
        existingUser.setId(1L);
        existingUser.setEmail("existing@example.com");
        existingUser.setLogin("existingLogin");
        existingUser.setName("Existing User");
        existingUser.setBirthday(dateFormat.parse("1990-01-01"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingUser)));

        invalidUserDto.setId(1L);
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
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

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)));
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
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "user123", "john_doe", "login"})
    void testUserController_addUser_WithValidLogins_ShouldSucceed(String login) throws Exception {
        validUserDto.setLogin(login);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login", is(login)));
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
    void testUserController_addUser_WithInlogins_ShouldReject(String login) throws Exception {
        validUserDto.setLogin(login);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1990-01-01",
            "2000-12-31",
            "2024-01-01",
            "2024-02-14"
    })
    void testUserController_addUser_WithValidBirthdays_ShouldSucceed(String dateString) throws Exception {
        Date birthday = dateFormat.parse(dateString);
        validUserDto.setBirthday(birthday);
        validUserDto.setBirthday(new Date(validUserDto.getBirthday().getTime() + 30000000));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthday", is(dateFormat.format(birthday))));
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
    }

    @ParameterizedTest
    @MethodSource("emailValidationProvider")
    void testUserController_addUser_WithVariousEmails_ShouldValidateCorrectly(String email, boolean shouldBeValid) throws Exception {
        validUserDto.setEmail(email);

        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDto)));

        if (shouldBeValid) {
            result.andExpect(status().isOk());
        } else {
            result.andExpect(status().isBadRequest());
        }
    }

    @ParameterizedTest
    @MethodSource("loginValidationProvider")
    void testUserController_addUser_WithVariousLogins_ShouldValidateCorrectly(String login, boolean shouldBeValid) throws Exception {
        validUserDto.setLogin(login);

        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDto)));

        if (shouldBeValid) {
            result.andExpect(status().isOk());
        } else {
            result.andExpect(status().isBadRequest());
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
