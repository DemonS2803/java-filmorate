package ru.yandex.practicum.filmorate.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateFilmRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private UpdateFilmRequestDto film;

    @BeforeEach
    void setup() {
        film = createValidFilm();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void testFilmDtoValidation_whenNameIsBlank(String invalidName) {
        film.setName(invalidName);

        Set<ConstraintViolation<UpdateFilmRequestDto>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @ParameterizedTest(name = "Дата релиза: {0} -> должна быть невалидной")
    @MethodSource("provideInvalidDates")
    void testFilmDtoValidation_whenReleaseDateIsInvalid(LocalDate invalidDate) {
        film.setReleaseDate(invalidDate);

        Set<ConstraintViolation<UpdateFilmRequestDto>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Фильм выпущен не ранее 18.12.1895")));
        assertEquals(1, violations.size());
    }

    @ParameterizedTest(name = "Дата релиза: {0} -> должна быть валидной")
    @MethodSource("provideValidDates")
    void testFilmDtoValidation_whenReleaseDateIsValid(LocalDate validDate) {
        film.setReleaseDate(validDate);

        Set<ConstraintViolation<UpdateFilmRequestDto>> violations = validator.validate(film);

        assertTrue(violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("releaseDate")));
    }

    private static Stream<LocalDate> provideInvalidDates() {
        return Stream.of(
                createDate(1895, Calendar.DECEMBER, 27),
                createDate(1895, Calendar.DECEMBER, 26),
                createDate(1895, Calendar.NOVEMBER, 1),
                createDate(1800, Calendar.JANUARY, 1),
                createDate(1000, Calendar.JUNE, 15),
                createDate(1894, Calendar.DECEMBER, 31)
        );
    }

    private static Stream<LocalDate> provideValidDates() {
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

    private static LocalDate createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        LocalDate ld = LocalDate.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).atStartOfDay().toLocalDate();
        return ld;
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("provideInvalidDurations")
    void testFilmDtoValidation_whenInvalidDuration(Integer invalidDuration) {
        film.setDuration(invalidDuration);

        Set<ConstraintViolation<UpdateFilmRequestDto>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }

    private static Stream<Arguments> provideInvalidDurations() {
        return Stream.of(
                Arguments.of(-10),
                Arguments.of(0)
        );
    }

    private UpdateFilmRequestDto createValidFilm() {
        UpdateFilmRequestDto film = new UpdateFilmRequestDto();
        film.setId(1L);
        film.setName("film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);
        film.setGenres(new ArrayList<>());
        film.setMpa(new FilmRatingDto(2, "PG_13"));
        return film;
    }

}
