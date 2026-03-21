package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ArrayList;
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
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequestDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequestDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
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

    private UpdateFilmRequestDto validUpdateFilmRequestDto;
    private Film validFilm;
    private FilmDto validFilmDto;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() throws Exception {
        validUpdateFilmRequestDto = new UpdateFilmRequestDto();
        validUpdateFilmRequestDto.setId(1L);
        validUpdateFilmRequestDto.setName("film");
        validUpdateFilmRequestDto.setDescription("description");
        validUpdateFilmRequestDto.setReleaseDate(LocalDate.parse("2000-01-01", dateFormat));
        validUpdateFilmRequestDto.setDuration(120);
        validUpdateFilmRequestDto.setMpa(new FilmRatingDto(2, "PG_13"));
        validUpdateFilmRequestDto.setGenres(new ArrayList<>());

        validFilm = new Film();
        validFilm.setId(1L);
        validFilm.setName("film");
        validFilm.setDescription("description");
        validFilm.setReleaseDate(LocalDate.parse("2000-01-01"));
        validFilm.setDuration(120);
        validFilm.setRating(new FilmRating(2, "PG-13"));
        validFilm.setGenres(new ArrayList<>());
        validFilmDto = FilmMapper.mapToFilmDto(validFilm);
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
                        .content(objectMapper.writeValueAsString(validUpdateFilmRequestDto)))
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

        verify(filmService, times(1)).createFilm(any(NewFilmRequestDto.class));
        verify(filmService, times(1)).getFilms();
    }

    @Test
    void testFilmController_updateFilm_ShouldUpdateExistingFilm() throws Exception {
        when(filmService.createFilm(any(NewFilmRequestDto.class))).thenReturn(validFilmDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateFilmRequestDto)))
                .andExpect(status().isCreated());

        UpdateFilmRequestDto updatedUpdateFilmRequestDto = new UpdateFilmRequestDto();
        updatedUpdateFilmRequestDto.setId(1L);
        updatedUpdateFilmRequestDto.setName("film edited");
        updatedUpdateFilmRequestDto.setDescription("descr edited");
        updatedUpdateFilmRequestDto.setReleaseDate(LocalDate.parse("2000-01-01"));
        updatedUpdateFilmRequestDto.setMpa(new FilmRatingDto(2, "PG_13"));
        updatedUpdateFilmRequestDto.setGenres(new ArrayList<>());
        updatedUpdateFilmRequestDto.setDuration(150);

        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("film edited");
        updatedFilm.setDescription("descr edited");
        updatedFilm.setReleaseDate(LocalDate.parse("2000-01-01"));
        updatedFilm.setDuration(150);
        updatedFilm.setRating(new FilmRating(2, "PG-13"));
        updatedFilm.setGenres(new ArrayList<>());
        FilmDto updatedFilmDto = FilmMapper.mapToFilmDto(updatedFilm);

        when(filmService.updateFilm(any(UpdateFilmRequestDto.class))).thenReturn(updatedFilmDto);

        List<FilmDto> filmsAfterUpdate = Collections.singletonList(updatedFilmDto);
        when(filmService.getFilms()).thenReturn(filmsAfterUpdate);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUpdateFilmRequestDto)))
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

        verify(filmService, times(1)).createFilm(any(NewFilmRequestDto.class));
        verify(filmService, times(1)).updateFilm(any(UpdateFilmRequestDto.class));
        verify(filmService, times(1)).getFilms();
    }

    @ParameterizedTest
    @MethodSource("invalidFilmProvider")
    void testFilmController_addFilm_WithInvalidData_ShouldReturnBadRequest(UpdateFilmRequestDto invalidUpdateFilmRequestDto) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateFilmRequestDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).createFilm(any(NewFilmRequestDto.class));
    }

    @ParameterizedTest
    @MethodSource("invalidFilmProvider")
    void testFilmController_updateFilm_WithInvalidData_ShouldReturnBadRequest(UpdateFilmRequestDto invalidUpdateFilmRequestDto) throws Exception {
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateFilmRequestDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).updateFilm(any(UpdateFilmRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 60, 120, 180, 300, 1000})
    void testFilmController_addFilm_WithValidDurations_ShouldSucceed(Integer duration) throws Exception {
        validUpdateFilmRequestDto.setDuration(duration);

        Film filmWithDuration = new Film();
        filmWithDuration.setId(1L);
        filmWithDuration.setName(validUpdateFilmRequestDto.getName());
        filmWithDuration.setDescription(validUpdateFilmRequestDto.getDescription());
        filmWithDuration.setReleaseDate(validUpdateFilmRequestDto.getReleaseDate());
        filmWithDuration.setDuration(duration);
        filmWithDuration.setRating(new FilmRating(2, "PG-13"));
        filmWithDuration.setGenres(new ArrayList<>());
        FilmDto filmWithDurationDto = FilmMapper.mapToFilmDto(filmWithDuration);

        when(filmService.createFilm(any(NewFilmRequestDto.class))).thenReturn(filmWithDurationDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateFilmRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duration", is(duration)));

        verify(filmService, times(1)).createFilm(any(NewFilmRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10, -100})
    void testFilmController_addFilm_WithInvalidDurations_ShouldReject(Integer duration) throws Exception {
        validUpdateFilmRequestDto.setDuration(duration);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateFilmRequestDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).createFilm(any(NewFilmRequestDto.class));
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
        validUpdateFilmRequestDto.setReleaseDate(releaseDate);

        Film filmWithDate = new Film();
        filmWithDate.setId(1L);
        filmWithDate.setName(validUpdateFilmRequestDto.getName());
        filmWithDate.setDescription(validUpdateFilmRequestDto.getDescription());
        filmWithDate.setReleaseDate(releaseDate);
        filmWithDate.setDuration(validUpdateFilmRequestDto.getDuration());
        filmWithDate.setRating(new FilmRating(2, "PG-13"));
        filmWithDate.setGenres(new ArrayList<>());
        FilmDto filmWithDateDto = FilmMapper.mapToFilmDto(filmWithDate);

        when(filmService.createFilm(any(NewFilmRequestDto.class))).thenReturn(filmWithDateDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateFilmRequestDto)))
                .andExpect(status().isCreated());

        verify(filmService, times(1)).createFilm(any(NewFilmRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1895-12-27",
            "1800-01-01",
            "1700-01-01"
    })
    void testFilmController_addFilm_WithInvalidReleaseDates_ShouldReject(String dateString) throws Exception {
        LocalDate releaseDate = LocalDate.parse(dateString);
        validUpdateFilmRequestDto.setReleaseDate(releaseDate);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateFilmRequestDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).createFilm(any(NewFilmRequestDto.class));
    }

    @ParameterizedTest
    @MethodSource("nameValidationProvider")
    void testFilmController_addFilm_WithVariousNames_ShouldValidateCorrectly(String name, boolean shouldBeValid) throws Exception {
        validUpdateFilmRequestDto.setName(name);

        if (shouldBeValid) {
            Film filmWithName = new Film();
            filmWithName.setId(1L);
            filmWithName.setName(name);
            filmWithName.setDescription(validUpdateFilmRequestDto.getDescription());
            filmWithName.setReleaseDate(validUpdateFilmRequestDto.getReleaseDate());
            filmWithName.setDuration(validUpdateFilmRequestDto.getDuration());
            filmWithName.setRating(new FilmRating(2, "PG-13"));
            filmWithName.setGenres(new ArrayList<>());
            FilmDto filmWithNameDto = FilmMapper.mapToFilmDto(filmWithName);

            when(filmService.createFilm(any(NewFilmRequestDto.class))).thenReturn(filmWithNameDto);
        }

        var result = mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateFilmRequestDto)));

        if (shouldBeValid) {
            result.andExpect(status().isCreated());
            verify(filmService, times(1)).createFilm(any(NewFilmRequestDto.class));
        } else {
            result.andExpect(status().isBadRequest());
            verify(filmService, never()).createFilm(any(NewFilmRequestDto.class));
        }
    }

    @ParameterizedTest
    @MethodSource("descriptionValidationProvider")
    void testFilmController_addFilm_WithVariousDescriptions_ShouldValidateCorrectly(String description, boolean shouldBeValid) throws Exception {
        validUpdateFilmRequestDto.setDescription(description);

        if (shouldBeValid) {
            Film filmWithDesc = new Film();
            filmWithDesc.setId(1L);
            filmWithDesc.setName(validUpdateFilmRequestDto.getName());
            filmWithDesc.setDescription(description);
            filmWithDesc.setReleaseDate(validUpdateFilmRequestDto.getReleaseDate());
            filmWithDesc.setDuration(validUpdateFilmRequestDto.getDuration());
            filmWithDesc.setRating(new FilmRating(2, "PG-13"));
            filmWithDesc.setGenres(new ArrayList<>());
            FilmDto filmWithDescDto = FilmMapper.mapToFilmDto(filmWithDesc);

            when(filmService.createFilm(any(NewFilmRequestDto.class))).thenReturn(filmWithDescDto);
        }

        var result = mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateFilmRequestDto)));

        if (shouldBeValid) {
            result.andExpect(status().isCreated());
            verify(filmService, times(1)).createFilm(any(NewFilmRequestDto.class));
        } else {
            result.andExpect(status().isBadRequest());
            verify(filmService, never()).createFilm(any(NewFilmRequestDto.class));
        }
    }

    private static Stream<Arguments> invalidFilmProvider() {
        UpdateFilmRequestDto nullNameFilm = new UpdateFilmRequestDto();
        nullNameFilm.setId(2L);
        nullNameFilm.setName(null);
        nullNameFilm.setDescription("Test");
        nullNameFilm.setReleaseDate(LocalDate.now());
        nullNameFilm.setDuration(120);

        UpdateFilmRequestDto emptyNameFilm = new UpdateFilmRequestDto();
        emptyNameFilm.setId(3L);
        emptyNameFilm.setName("");
        emptyNameFilm.setDescription("Test");
        emptyNameFilm.setReleaseDate(LocalDate.now());
        emptyNameFilm.setDuration(120);

        UpdateFilmRequestDto blankNameFilm = new UpdateFilmRequestDto();
        blankNameFilm.setId(4L);
        blankNameFilm.setName("   ");
        blankNameFilm.setDescription("Test");
        blankNameFilm.setReleaseDate(LocalDate.now());
        blankNameFilm.setDuration(120);

        UpdateFilmRequestDto nullReleaseDateFilm = new UpdateFilmRequestDto();
        nullReleaseDateFilm.setId(5L);
        nullReleaseDateFilm.setName("Test");
        nullReleaseDateFilm.setDescription("Test");
        nullReleaseDateFilm.setReleaseDate(null);
        nullReleaseDateFilm.setDuration(120);

        UpdateFilmRequestDto nullDurationFilm = new UpdateFilmRequestDto();
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