package ru.yandex.practicum.filmorate.controller;

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
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmGenreFoundException;
import ru.yandex.practicum.filmorate.service.FilmGenreService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FIlmGenreController.class)
class FilmGenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmGenreService filmGenreService;

    private List<FilmGenreDto> mockGenres;
    private FilmGenreDto comedyGenre;
    private FilmGenreDto dramaGenre;
    private FilmGenreDto actionGenre;

    @BeforeEach
    void setUp() {
        // Initialize mock DTOs based on the example in the requirement
        comedyGenre = new FilmGenreDto(1L, "Комедия");
        dramaGenre = new FilmGenreDto(2L, "Драма");
        actionGenre = new FilmGenreDto(3L, "Боевик");

        mockGenres = Arrays.asList(comedyGenre, dramaGenre, actionGenre);
    }

    @Test
    void getFilmGenres_ShouldReturnAllGenres() throws Exception {
        // Given
        when(filmGenreService.getAllFilmGenres()).thenReturn(mockGenres);

        // When & Then
        mockMvc.perform(get("/genres")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Комедия"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Драма"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("Боевик"));

        verify(filmGenreService, times(1)).getAllFilmGenres();
    }

    @Test
    void getFilmGenres_WhenServiceReturnsEmptyList_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(filmGenreService.getAllFilmGenres()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/genres")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(filmGenreService, times(1)).getAllFilmGenres();
    }

    @Test
    void getFilmGenreById_WithValidId_ShouldReturnGenre() throws Exception {
        // Given
        int id = 1;
        when(filmGenreService.getFilmGenreById(id)).thenReturn(comedyGenre);

        // When & Then
        mockMvc.perform(get("/genres/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"));

        verify(filmGenreService, times(1)).getFilmGenreById(id);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void getFilmGenreById_WithAllValidIds_ShouldReturnCorrespondingGenres(int id) throws Exception {
        // Given
        FilmGenreDto expectedGenre = switch (id) {
            case 1 -> comedyGenre;
            case 2 -> dramaGenre;
            case 3 -> actionGenre;
            default -> null;
        };

        when(filmGenreService.getFilmGenreById(id)).thenReturn(expectedGenre);

        // When & Then
        mockMvc.perform(get("/genres/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        String expectedName = switch (id) {
            case 1 -> "Комедия";
            case 2 -> "Драма";
            case 3 -> "Боевик";
            default -> "";
        };

        mockMvc.perform(get("/genres/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(expectedName));

        verify(filmGenreService, times(2)).getFilmGenreById(id);
    }

    @Test
    void getFilmGenreById_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        int invalidId = 99;
        when(filmGenreService.getFilmGenreById(invalidId))
                .thenThrow(new NoFilmGenreFoundException("Film genre not found with id: " + invalidId));

        // When & Then
        mockMvc.perform(get("/genres/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(filmGenreService, times(1)).getFilmGenreById(invalidId);
    }

    @Test
    void getFilmGenreById_WithNegativeId_ShouldReturn404() throws Exception {
        // Given
        int negativeId = -1;
        when(filmGenreService.getFilmGenreById(negativeId))
                .thenThrow(new NoFilmGenreFoundException("Film genre not found with id: " + negativeId));

        // When & Then
        mockMvc.perform(get("/genres/{id}", negativeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(filmGenreService, times(1)).getFilmGenreById(negativeId);
    }

    @Test
    void getFilmGenreById_WithZeroId_ShouldReturn404() throws Exception {
        // Given
        int zeroId = 0;
        when(filmGenreService.getFilmGenreById(zeroId))
                .thenThrow(new NoFilmGenreFoundException("Film genre not found with id: " + zeroId));

        // When & Then
        mockMvc.perform(get("/genres/{id}", zeroId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(filmGenreService, times(1)).getFilmGenreById(zeroId);
    }

    @Test
    void getFilmGenreById_WithStringId_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/genres/{id}", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(filmGenreService, never()).getFilmGenreById(anyInt());
    }

    @Test
    void getFilmGenres_ShouldReturnCorrectJsonStructure() throws Exception {
        // Given
        when(filmGenreService.getAllFilmGenres()).thenReturn(mockGenres);

        // When & Then - Verify JSON structure matches the example in requirements
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].description").doesNotExist()); // Ensure no unexpected fields
    }

    @Test
    void getFilmGenreById_ShouldReturnCorrectJsonStructure() throws Exception {
        // Given
        when(filmGenreService.getFilmGenreById(1)).thenReturn(comedyGenre);

        // When & Then - Verify JSON structure matches the example in requirements
        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"))
                .andExpect(jsonPath("$.description").doesNotExist()); // Ensure no unexpected fields
    }

    @Test
    void getFilmGenreById_WithVeryLargeId_ShouldReturn404() throws Exception {
        // Given
        int largeId = 999999;
        when(filmGenreService.getFilmGenreById(largeId))
                .thenThrow(new NoFilmGenreFoundException("Film genre not found with id: " + largeId));

        // When & Then
        mockMvc.perform(get("/genres/{id}", largeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(filmGenreService, times(1)).getFilmGenreById(largeId);
    }

    @Test
    void controllerEndpoints_ShouldHandleServiceException() throws Exception {
        // Given
        when(filmGenreService.getAllFilmGenres()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/genres"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getFilmGenreById_WithMultipleRequests_ShouldCallServiceEachTime() throws Exception {
        // Given
        when(filmGenreService.getFilmGenreById(1)).thenReturn(comedyGenre);
        when(filmGenreService.getFilmGenreById(2)).thenReturn(dramaGenre);

        // When
        mockMvc.perform(get("/genres/1"));
        mockMvc.perform(get("/genres/2"));
        mockMvc.perform(get("/genres/1"));

        // Then
        verify(filmGenreService, times(2)).getFilmGenreById(1);
        verify(filmGenreService, times(1)).getFilmGenreById(2);
        verify(filmGenreService, times(3)).getFilmGenreById(anyInt());
    }

    @Test
    void getFilmGenres_ShouldReturnGenresInCorrectOrder() throws Exception {
        // Given
        List<FilmGenreDto> orderedGenres = Arrays.asList(
                new FilmGenreDto(1L, "Комедия"),
                new FilmGenreDto(2L, "Драма"),
                new FilmGenreDto(3L, "Боевик"),
                new FilmGenreDto(4L, "Триллер"),
                new FilmGenreDto(5L, "Ужасы")
        );
        when(filmGenreService.getAllFilmGenres()).thenReturn(orderedGenres);

        // When & Then
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Комедия"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Драма"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("Боевик"))
                .andExpect(jsonPath("$[3].id").value(4))
                .andExpect(jsonPath("$[3].name").value("Триллер"))
                .andExpect(jsonPath("$[4].id").value(5))
                .andExpect(jsonPath("$[4].name").value("Ужасы"));
    }

    @Test
    void getFilmGenreById_WithId1_ShouldReturnComedy() throws Exception {
        // Given
        when(filmGenreService.getFilmGenreById(1)).thenReturn(comedyGenre);

        // When & Then - Verify specific example from requirements
        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"));
    }
}