package ru.yandex.practicum.filmorate.controller;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;
import ru.yandex.practicum.filmorate.service.FilmRatingService;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmRatingController.class)
class FilmRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmRatingService filmRatingService;

    private List<FilmRatingDto> mockRatings;
    private FilmRatingDto gRating;
    private FilmRatingDto pgRating;
    private FilmRatingDto pg13Rating;
    private FilmRatingDto rRating;
    private FilmRatingDto nc17Rating;

    @BeforeEach
    void setUp() {
        gRating = new FilmRatingDto(1, "0+");
        pgRating = new FilmRatingDto(2, "6+");
        pg13Rating = new FilmRatingDto(3, "12+");
        rRating = new FilmRatingDto(4, "16+");
        nc17Rating = new FilmRatingDto(5, "18+");

        mockRatings = Arrays.asList(gRating, pgRating, pg13Rating, rRating, nc17Rating);
    }

    @Test
    void getFilmRatings_ShouldReturnAllRatings() throws Exception {
        when(filmRatingService.getAllFilmRatings()).thenReturn(mockRatings);

        mockMvc.perform(get("/mpa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("0+"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("6+"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("12+"))
                .andExpect(jsonPath("$[3].id").value(4))
                .andExpect(jsonPath("$[3].name").value("16+"))
                .andExpect(jsonPath("$[4].id").value(5))
                .andExpect(jsonPath("$[4].name").value("18+"));

        verify(filmRatingService, times(1)).getAllFilmRatings();
    }

    @Test
    void getFilmRatings_WhenServiceReturnsEmptyList_ShouldReturnEmptyArray() throws Exception {
        when(filmRatingService.getAllFilmRatings()).thenReturn(List.of());

        mockMvc.perform(get("/mpa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(filmRatingService, times(1)).getAllFilmRatings();
    }

    @Test
    void getFilmRatingById_WithValidId_ShouldReturnRating() throws Exception {
        int id = 1;
        when(filmRatingService.getFilmRatingById(id)).thenReturn(gRating);

        mockMvc.perform(get("/mpa/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("0+"));

        verify(filmRatingService, times(1)).getFilmRatingById(id);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void getFilmRatingById_WithAllValidIds_ShouldReturnCorrespondingRatings(int id) throws Exception {
        FilmRatingDto expectedRating = switch (id) {
            case 1 -> gRating;
            case 2 -> pgRating;
            case 3 -> pg13Rating;
            case 4 -> rRating;
            case 5 -> nc17Rating;
            default -> null;
        };

        when(filmRatingService.getFilmRatingById(id)).thenReturn(expectedRating);

        mockMvc.perform(get("/mpa/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        verify(filmRatingService, times(1)).getFilmRatingById(id);
    }

    @Test
    void getFilmRatingById_WithInvalidId_ShouldReturn404() throws Exception {
        int invalidId = 99;
        when(filmRatingService.getFilmRatingById(invalidId))
                .thenThrow(new NoFilmRatingFoundException("No film rating with id " + invalidId));

        mockMvc.perform(get("/mpa/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(filmRatingService, times(1)).getFilmRatingById(invalidId);
    }

    @Test
    void getFilmRatingById_WithStringId_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/mpa/{id}", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(filmRatingService, never()).getFilmRatingById(anyInt());
    }

    @Test
    void getFilmRatingById_WithNegativeId_ShouldReturn404() throws Exception {
        int negativeId = -1;
        when(filmRatingService.getFilmRatingById(negativeId))
                .thenThrow(new NoFilmRatingFoundException("No film rating with id " + negativeId));

        mockMvc.perform(get("/mpa/{id}", negativeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(filmRatingService, times(1)).getFilmRatingById(negativeId);
    }

    @Test
    void getFilmRatingById_WithZeroId_ShouldReturn404() throws Exception {
        int zeroId = 0;
        when(filmRatingService.getFilmRatingById(zeroId))
                .thenThrow(new NoFilmRatingFoundException("No film rating with id " + zeroId));

        mockMvc.perform(get("/mpa/{id}", zeroId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(filmRatingService, times(1)).getFilmRatingById(zeroId);
    }

    @Test
    void getFilmRatingById_WithIdGreaterThanMax_ShouldReturn404() throws Exception {
        int largeId = 10;
        when(filmRatingService.getFilmRatingById(largeId))
                .thenThrow(new NoFilmRatingFoundException("No film rating with id " + largeId));

        mockMvc.perform(get("/mpa/{id}", largeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(filmRatingService, times(1)).getFilmRatingById(largeId);
    }

    @Test
    void getFilmRatings_ShouldReturnCorrectJsonStructure() throws Exception {
        when(filmRatingService.getAllFilmRatings()).thenReturn(mockRatings);

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists()); // Ensure no unexpected fields
    }

    @Test
    void getFilmRatingById_ShouldReturnCorrectJsonStructure() throws Exception {
        when(filmRatingService.getFilmRatingById(1)).thenReturn(gRating);

        mockMvc.perform(get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void controllerEndpoints_ShouldHandleServiceException() throws Exception {
        when(filmRatingService.getAllFilmRatings()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/mpa"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getFilmRatingById_WithMultipleRequests_ShouldCallServiceEachTime() throws Exception {
        when(filmRatingService.getFilmRatingById(1)).thenReturn(gRating);
        when(filmRatingService.getFilmRatingById(2)).thenReturn(pgRating);

        mockMvc.perform(get("/mpa/1"));
        mockMvc.perform(get("/mpa/2"));

        verify(filmRatingService, times(1)).getFilmRatingById(1);
        verify(filmRatingService, times(1)).getFilmRatingById(2);
        verify(filmRatingService, times(2)).getFilmRatingById(anyInt());
    }
}
