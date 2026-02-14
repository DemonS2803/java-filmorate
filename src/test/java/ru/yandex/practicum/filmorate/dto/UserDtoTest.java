package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class UserDtoTest {

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private UserDto user;

    @BeforeEach
    void setup() {
        user = createValidUser();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void testUserDtoValidation_whenEmailIsBlank(String invalidEmail) {
        user.setEmail(invalidEmail);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        && v.getMessage().equals("Электронная почта не может быть пустой")));
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
    void testUserDtoValidation_whenEmailIsInvalid(String invalidEmail) {
        user.setEmail(invalidEmail);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        && v.getMessage().equals("Неверный формат электронной почты")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "123@example.com"
    })
    void testUserDtoValidation_whenEmailIsValid(String validEmail) {
        user.setEmail(validEmail);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void testUserDtoValidation_whenLoginIsBlank(String invalidLogin) {
        user.setLogin(invalidLogin);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login")
                        && v.getMessage().equals("Логин не может быть пустым")));
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
    void testUserDtoValidation_whenLoginContainsWhitespace(String invalidLogin) {
        user.setLogin(invalidLogin);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login")
                        && v.getMessage().equals("Логин не должен содержать пробельные символы")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"validLogin", "user123", "john_doe", "login"})
    void testUserDtoValidation_whenLoginIsValid(String validLogin) {
        user.setLogin(validLogin);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("login")));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBirthdays")
    void testUserDtoValidation_whenBirthdayIsInvalid(Date invalidBirthday) {
        user.setBirthday(invalidBirthday);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday")
                        && v.getMessage().equals("День рождения не может быть в будущем")));
        assertEquals(1, violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("birthday"))
                .count());
    }

    @ParameterizedTest
    @MethodSource("provideValidBirthdays")
    void testUserDtoValidation_whenBirthdayIsValid(Date validBirthday) {
        user.setBirthday(validBirthday);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("birthday")));
    }

    private static Stream<Date> provideValidBirthdays() {
        return Stream.of(
                getDate(2000, 1, 1),
                getDate(1995, 5, 15),
                getDate(1980, 12, 31),
                getDate(2024, 1, 1),
                getDate(2024, 2, 14),
                null
        );
    }

    private static Stream<Date> provideInvalidBirthdays() {
        return Stream.of(
                getDate(2126, 1, 1),
                getDate(2030, 12, 31),
                getDate(2027, 5, 20)
        );
    }

    private static Date getDate(int year, int month, int day) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(String.format("%d-%02d-%02d", year, month, day));
        } catch (ParseException e) {
            fail("Failed to parse date: " + e.getMessage());
            return null;
        }
    }

    private UserDto createValidUser() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("John Doe");
        user.setBirthday(getDate(1990, 1, 1));
        return user;
    }
}