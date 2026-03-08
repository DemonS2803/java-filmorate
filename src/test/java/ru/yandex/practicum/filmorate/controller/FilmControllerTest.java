package ru.yandex.practicum.filmorate.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.FilmRatingMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.service.FilmRatingService;
import ru.yandex.practicum.filmorate.service.FilmService;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({FilmController.class, ExceptionHandlerController.class})
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilmService filmService;

    @Autowired
    private ObjectMapper objectMapper;

    private FilmDto validFilmDto;
    private Film validFilm;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() throws Exception {
        validFilmDto = new FilmDto();
        validFilmDto.setId(1L);
        validFilmDto.setName("film");
        validFilmDto.setDescription("description");
        validFilmDto.setReleaseDate(LocalDate.parse("2000-01-01", dateFormat));
        validFilmDto.setDuration(120);
        validFilmDto.setMpa(FilmRatingMapper.toDto(FilmRating.PG_13, FilmRatingService.FILM_RATING_LOCALE));
        validFilmDto.setGenres(new ArrayList<>());

        validFilm = new Film();
        validFilm.setId(1L);
        validFilm.setName("film");
        validFilm.setDescription("description");
        validFilm.setReleaseDate(LocalDate.parse("2000-01-01"));
        validFilm.setDuration(120);
        validFilm.setRating(FilmRating.PG_13);
        validFilm.setGenres(new HashSet<>());
    }

    @Test
    void testFilmController_getFilms_ShouldReturnEmptyList() throws Exception {
        when(filmService.getFilms()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(filmService, times(1)).getFilms();
    }

    @Test
    void testFilmController_addFilm_And_GetFilms_ShouldWorkTogether() throws Exception {
        when(filmService.createFilm(any())).thenReturn(validFilmDto);

        List<FilmDto> filmsList = Collections.singletonList(validFilmDto);
        when(filmService.getFilms()).thenReturn(filmsList);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("film")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.duration", is(120)));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("film")));

        verify(filmService, times(1)).createFilm(any(Film.class));
        verify(filmService, times(1)).getFilms();
    }

    @Test
    void testFilmController_updateFilm_ShouldUpdateExistingFilm() throws Exception {
        when(filmService.createFilm(any(Film.class))).thenReturn(validFilmDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isCreated());

        FilmDto updatedFilmDto = new FilmDto();
        updatedFilmDto.setId(1L);
        updatedFilmDto.setName("film edited");
        updatedFilmDto.setDescription("descr edited");
        updatedFilmDto.setReleaseDate(LocalDate.parse("2000-01-01"));
        updatedFilmDto.setMpa(FilmRatingMapper.toDto(FilmRating.PG_13, FilmRatingService.FILM_RATING_LOCALE));
        updatedFilmDto.setGenres(new ArrayList<>());
        updatedFilmDto.setDuration(150);

        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("film edited");
        updatedFilm.setDescription("descr edited");
        updatedFilm.setReleaseDate(LocalDate.parse("2000-01-01"));
        updatedFilm.setDuration(150);
        updatedFilm.setRating(FilmRating.PG_13);
        updatedFilm.setGenres(new HashSet<>());

        when(filmService.updateFilm(any(Film.class))).thenReturn(updatedFilmDto);

        List<FilmDto> filmsAfterUpdate = Collections.singletonList(updatedFilmDto);
        when(filmService.getFilms()).thenReturn(filmsAfterUpdate);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilmDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("film edited")))
                .andExpect(jsonPath("$.description", is("descr edited")))
                .andExpect(jsonPath("$.releaseDate", is("2000-01-01")))
                .andExpect(jsonPath("$.duration", is(150)));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("film edited")))
                .andExpect(jsonPath("$[0].description", is("descr edited")));

        verify(filmService, times(1)).createFilm(any(Film.class));
        verify(filmService, times(1)).updateFilm(any(Film.class));
        verify(filmService, times(1)).getFilms();
    }

    @ParameterizedTest
    @MethodSource("invalidFilmProvider")
    void testFilmController_addFilm_WithInvalidData_ShouldReturnBadRequest(FilmDto invalidFilmDto) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilmDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).createFilm(any(Film.class));
    }

    @ParameterizedTest
    @MethodSource("invalidFilmProvider")
    void testFilmController_updateFilm_WithInvalidData_ShouldReturnBadRequest(FilmDto invalidFilmDto) throws Exception {
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilmDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).updateFilm(any(Film.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 60, 120, 180, 300, 1000})
    void testFilmController_addFilm_WithValidDurations_ShouldSucceed(Integer duration) throws Exception {
        validFilmDto.setDuration(duration);

        Film filmWithDuration = new Film();
        filmWithDuration.setId(1L);
        filmWithDuration.setName(validFilmDto.getName());
        filmWithDuration.setDescription(validFilmDto.getDescription());
        filmWithDuration.setReleaseDate(validFilmDto.getReleaseDate());
        filmWithDuration.setDuration(duration);
        filmWithDuration.setRating(FilmRating.PG_13);
        filmWithDuration.setGenres(new HashSet<>());
        FilmDto filmWithDurationDto = FilmMapper.mapToFilmDto(filmWithDuration);

        when(filmService.createFilm(any(Film.class))).thenReturn(filmWithDurationDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duration", is(duration)));

        verify(filmService, times(1)).createFilm(any(Film.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10, -100})
    void testFilmController_addFilm_WithInvalidDurations_ShouldReject(Integer duration) throws Exception {
        validFilmDto.setDuration(duration);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).createFilm(any(Film.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1895-12-29",
            "1900-01-01",
            "2000-01-01",
            "2024-01-01"
    })
    void testFilmController_addFilm_WithValidReleaseDates_ShouldSucceed(String dateString) throws Exception {
        LocalDate releaseDate = LocalDate.parse(dateString);
        validFilmDto.setReleaseDate(releaseDate);

        Film filmWithDate = new Film();
        filmWithDate.setId(1L);
        filmWithDate.setName(validFilmDto.getName());
        filmWithDate.setDescription(validFilmDto.getDescription());
        filmWithDate.setReleaseDate(releaseDate);
        filmWithDate.setDuration(validFilmDto.getDuration());
        filmWithDate.setRating(FilmRating.PG_13);
        filmWithDate.setGenres(new HashSet<>());
        FilmDto filmWithDateDto = FilmMapper.mapToFilmDto(filmWithDate);

        when(filmService.createFilm(any(Film.class))).thenReturn(filmWithDateDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isCreated());

        verify(filmService, times(1)).createFilm(any(Film.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1895-12-27",
            "1800-01-01",
            "1700-01-01"
    })
    void testFilmController_addFilm_WithInvalidReleaseDates_ShouldReject(String dateString) throws Exception {
        LocalDate releaseDate = LocalDate.parse(dateString);
        validFilmDto.setReleaseDate(releaseDate);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilmDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).createFilm(any(Film.class));
    }

    @ParameterizedTest
    @MethodSource("nameValidationProvider")
    void testFilmController_addFilm_WithVariousNames_ShouldValidateCorrectly(String name, boolean shouldBeValid) throws Exception {
        validFilmDto.setName(name);

        if (shouldBeValid) {
            Film filmWithName = new Film();
            filmWithName.setId(1L);
            filmWithName.setName(name);
            filmWithName.setDescription(validFilmDto.getDescription());
            filmWithName.setReleaseDate(validFilmDto.getReleaseDate());
            filmWithName.setDuration(validFilmDto.getDuration());
            filmWithName.setRating(FilmRating.PG_13);
            filmWithName.setGenres(new HashSet<>());
            FilmDto filmWithNameDto = FilmMapper.mapToFilmDto(filmWithName);

            when(filmService.createFilm(any(Film.class))).thenReturn(filmWithNameDto);
        }

        var result = mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilmDto)));

        if (shouldBeValid) {
            result.andExpect(status().isCreated());
            verify(filmService, times(1)).createFilm(any(Film.class));
        } else {
            result.andExpect(status().isBadRequest());
            verify(filmService, never()).createFilm(any(Film.class));
        }
    }

    @ParameterizedTest
    @MethodSource("descriptionValidationProvider")
    void testFilmController_addFilm_WithVariousDescriptions_ShouldValidateCorrectly(String description, boolean shouldBeValid) throws Exception {
        validFilmDto.setDescription(description);

        if (shouldBeValid) {
            Film filmWithDesc = new Film();
            filmWithDesc.setId(1L);
            filmWithDesc.setName(validFilmDto.getName());
            filmWithDesc.setDescription(description);
            filmWithDesc.setReleaseDate(validFilmDto.getReleaseDate());
            filmWithDesc.setDuration(validFilmDto.getDuration());
            filmWithDesc.setRating(FilmRating.PG_13);
            filmWithDesc.setGenres(new HashSet<>());
            FilmDto filmWithDescDto = FilmMapper.mapToFilmDto(filmWithDesc);

            when(filmService.createFilm(any(Film.class))).thenReturn(filmWithDescDto);
        }

        var result = mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilmDto)));

        if (shouldBeValid) {
            result.andExpect(status().isCreated());
            verify(filmService, times(1)).createFilm(any(Film.class));
        } else {
            result.andExpect(status().isBadRequest());
            verify(filmService, never()).createFilm(any(Film.class));
        }
    }

    private static Stream<Arguments> invalidFilmProvider() {
        FilmDto nullNameFilm = new FilmDto();
        nullNameFilm.setId(2L);
        nullNameFilm.setName(null);
        nullNameFilm.setDescription("Test");
        nullNameFilm.setReleaseDate(LocalDate.now());
        nullNameFilm.setDuration(120);

        FilmDto emptyNameFilm = new FilmDto();
        emptyNameFilm.setId(3L);
        emptyNameFilm.setName("");
        emptyNameFilm.setDescription("Test");
        emptyNameFilm.setReleaseDate(LocalDate.now());
        emptyNameFilm.setDuration(120);

        FilmDto blankNameFilm = new FilmDto();
        blankNameFilm.setId(4L);
        blankNameFilm.setName("   ");
        blankNameFilm.setDescription("Test");
        blankNameFilm.setReleaseDate(LocalDate.now());
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
        nullDurationFilm.setReleaseDate(LocalDate.now());
        nullDurationFilm.setDuration(null);

        return Stream.of(
                Arguments.of(nullNameFilm),
                Arguments.of(emptyNameFilm),
                Arguments.of(blankNameFilm),
                Arguments.of(nullReleaseDateFilm),
                Arguments.of(nullDurationFilm)
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