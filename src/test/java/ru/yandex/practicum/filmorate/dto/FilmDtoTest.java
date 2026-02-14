package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmDtoTest {

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private FilmDto film;

    @BeforeEach
    void setup() {
        film = createValidFilm();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void testFilmDtoValidation_whenNameIsBlank(String invalidName) {
        film.setName(invalidName);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @ParameterizedTest(name = "Дата релиза: {0} -> должна быть невалидной")
    @MethodSource("provideInvalidDates")
    void testFilmDtoValidation_whenReleaseDateIsInvalid(Date invalidDate) {
        film.setReleaseDate(invalidDate);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Фильм выпущен не ранее 18.12.1895")));
        assertEquals(1, violations.size());
    }

    @ParameterizedTest(name = "Дата релиза: {0} -> должна быть валидной")
    @MethodSource("provideValidDates")
    void testFilmDtoValidation_whenReleaseDateIsValid(Date validDate) {
        film.setReleaseDate(validDate);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);

        assertTrue(violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("releaseDate")));
    }

    private static Stream<Date> provideInvalidDates() {
        return Stream.of(
                createDate(1895, Calendar.DECEMBER, 27),
                createDate(1895, Calendar.DECEMBER, 26),
                createDate(1895, Calendar.NOVEMBER, 1),
                createDate(1800, Calendar.JANUARY, 1),
                createDate(1000, Calendar.JUNE, 15),
                createDate(1894, Calendar.DECEMBER, 31)
        );
    }

    private static Stream<Date> provideValidDates() {
        return Stream.of(
                createDate(1895, Calendar.DECEMBER, 28),
                createDate(1895, Calendar.DECEMBER, 29),
                createDate(1895, Calendar.DECEMBER, 30),
                createDate(1895, Calendar.DECEMBER, 31),
                createDate(1896, Calendar.JANUARY, 1),
                createDate(1900, Calendar.JANUARY, 1),
                createDate(2000, Calendar.JANUARY, 1),
                createDate(2024, Calendar.FEBRUARY, 14)
        );
    }

    private static Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDurations")
    void testFilmDtoValidation_whenInvalidDuration(Integer invalidDuration) {
        film.setDuration(invalidDuration);

        Set<ConstraintViolation<FilmDto>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }

    private static Stream<Arguments> provideInvalidDurations() {
        return Stream.of(
                Arguments.of(-10, " быть положительной"),
                Arguments.of(0, " быть положительной"),
                Arguments.of(null, " быть положительной")
        );
    }

    private FilmDto createValidFilm() {
        FilmDto film = new FilmDto();
        film.setId(1L);
        film.setName("film");
        film.setDescription("description");
        film.setReleaseDate(new Date());
        film.setDuration(120);
        return film;
    }

}
