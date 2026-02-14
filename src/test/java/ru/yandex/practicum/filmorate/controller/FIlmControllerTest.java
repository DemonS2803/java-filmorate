package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import ru.yandex.practicum.filmorate.dto.FilmDto;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilmController filmController;

    @Autowired
    private ObjectMapper objectMapper;

    private FilmDto validFilmDto;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        validFilmDto = new FilmDto();
        validFilmDto.setId(1L);
        validFilmDto.setName("film");
        validFilmDto.setDescription("description");

        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.JANUARY, 1);
        validFilmDto.setReleaseDate(cal.getTime());
        validFilmDto.setDuration(120);

        ReflectionTestUtils.setField(filmController, "films", new HashMap<>());
    }

    @Test
    void testFilmController_getFilms_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testFilmController_addFilm_And_GetFilms_ShouldWorkTogether() throws Exception {
        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("film")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.duration", is(120)));

        mockMvc.perform(get("/api/v1/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("film")));
    }

    @Test
    void testFilmController_updateFilm_ShouldUpdateExistingFilm() throws Exception {
        mockMvc.perform(post("/api/v1/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilmDto)));

        validFilmDto.setName("film edited");
        validFilmDto.setDescription("descr edited");
        validFilmDto.setDuration(150);

        mockMvc.perform(put("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("film edited")))
                .andExpect(jsonPath("$.description", is("descr edited")))
                .andExpect(jsonPath("$.duration", is(150)));

        mockMvc.perform(get("/api/v1/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("film edited")))
                .andExpect(jsonPath("$[0].description", is("descr edited")));
    }

    @ParameterizedTest
    @MethodSource("invalidFilmProvider")
    void testFilmController_addFilm_WithInvalidData_ShouldReturnBadRequest(FilmDto invalidFilmDto) throws Exception {
        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilmDto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("invalidFilmProvider")
    void testFilmController_updateFilm_WithInvalidData_ShouldReturnBadRequest(FilmDto invalidFilmDto) throws Exception {
        mockMvc.perform(put("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilmDto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 60, 120, 180, 300, 1000})
    void testFilmController_addFilm_WithValidDurations_ShouldSucceed(Integer duration) throws Exception {
        validFilmDto.setDuration(duration);

        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration", is(duration)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10, -100})
    void testFilmController_addFilm_WithInvalidDurations_ShouldReject(Integer duration) throws Exception {
        validFilmDto.setDuration(duration);

        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("validReleaseDateProvider")
    void testFilmController_addFilm_WithValidReleaseDates_ShouldSucceed(String dateString) throws Exception {
        Date releaseDate = dateFormat.parse(dateString);
        validFilmDto.setReleaseDate(releaseDate);

        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "Release date: {0} should be invalid")
    @MethodSource("invalidReleaseDateProvider")
    @DisplayName("Should reject invalid release dates")
    void testFilmController_addFilm_WithInvalidReleaseDates_ShouldReject(String dateString) throws Exception {
        Date releaseDate = dateFormat.parse(dateString);
        validFilmDto.setReleaseDate(releaseDate);

        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "Name: '{0}' should be {1}")
    @MethodSource("nameValidationProvider")
    @DisplayName("Should validate film names correctly")
    void testFilmController_addFilm_WithVariousNames_ShouldValidateCorrectly(String name, boolean shouldBeValid) throws Exception {
        validFilmDto.setName(name);

        var result = mockMvc.perform(post("/api/v1/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilmDto)));

        if (shouldBeValid) {
            result.andExpect(status().isOk());
        } else {
            result.andExpect(status().isBadRequest());
        }
    }

    @ParameterizedTest(name = "Description length: {0}")
    @MethodSource("descriptionValidationProvider")
    void testFilmController_addFilm_WithVariousDescriptions_ShouldValidateCorrectly(String description, boolean shouldBeValid) throws Exception {
        validFilmDto.setDescription(description);

        var result = mockMvc.perform(post("/api/v1/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilmDto)));

        if (shouldBeValid) {
            result.andExpect(status().isOk());
        } else {
            result.andExpect(status().isBadRequest());
        }
    }

    private static Stream<Arguments> invalidFilmProvider() {
        FilmDto nullNameFilm = new FilmDto();
        nullNameFilm.setId(2L);
        nullNameFilm.setName(null);
        nullNameFilm.setDescription("Test");
        nullNameFilm.setReleaseDate(new Date());
        nullNameFilm.setDuration(120);

        FilmDto emptyNameFilm = new FilmDto();
        emptyNameFilm.setId(3L);
        emptyNameFilm.setName("");
        emptyNameFilm.setDescription("Test");
        emptyNameFilm.setReleaseDate(new Date());
        emptyNameFilm.setDuration(120);

        FilmDto blankNameFilm = new FilmDto();
        blankNameFilm.setId(4L);
        blankNameFilm.setName("   ");
        blankNameFilm.setDescription("Test");
        blankNameFilm.setReleaseDate(new Date());
        blankNameFilm.setDuration(120);

        FilmDto nullReleaseDateFilm = new FilmDto();
        nullReleaseDateFilm.setId(5L);
        nullReleaseDateFilm.setName("Test");
        nullReleaseDateFilm.setDescription("Test");
        nullReleaseDateFilm.setReleaseDate(null);
        nullReleaseDateFilm.setDuration(120);

        FilmDto nullDurationFilm = new FilmDto();
        nullDurationFilm.setId(6L);
        nullDurationFilm.setName("Test");
        nullDurationFilm.setDescription("Test");
        nullDurationFilm.setReleaseDate(new Date());
        nullDurationFilm.setDuration(null);

        return Stream.of(
                Arguments.of(nullNameFilm),
                Arguments.of(emptyNameFilm),
                Arguments.of(blankNameFilm),
                Arguments.of(nullReleaseDateFilm),
                Arguments.of(nullDurationFilm)
        );
    }

    private static Stream<Arguments> validReleaseDateProvider() {
        return Stream.of(
                Arguments.of("1895-12-29"),
                Arguments.of("1900-01-01"),
                Arguments.of("2000-01-01"),
                Arguments.of("2024-01-01")
        );
    }

    private static Stream<Arguments> invalidReleaseDateProvider() {
        return Stream.of(
                Arguments.of("1895-12-27"),
                Arguments.of("1895-12-28"),
                Arguments.of("1800-01-01"),
                Arguments.of("1700-01-01")
        );
    }

    private static Stream<Arguments> nameValidationProvider() {
        return Stream.of(
                Arguments.of("Valid Name", true),
                Arguments.of("A", true),
                Arguments.of("123", true),
                Arguments.of("Name with spaces", true),
                Arguments.of("Special!@#", true),
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of("   ", false)
        );
    }

    private static Stream<Arguments> descriptionValidationProvider() {
        String validDescription = "A".repeat(200);
        String invalidDescription = "A".repeat(201);

        return Stream.of(
                Arguments.of(null, true), // description is optional
                Arguments.of("", true),
                Arguments.of("Short description", true),
                Arguments.of(validDescription, true),
                Arguments.of(invalidDescription, false)
        );
    }
}