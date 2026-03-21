package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmRatingStorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FilmRatingServiceTest {

    @InjectMocks
    private FilmRatingService filmRatingService;

    @Mock
    private FilmRatingStorage filmRatingStorage;

    List<FilmRating> ratings;

    @BeforeEach
    void setUp() {
        ratings = List.of(
                new FilmRating(1, "G"),
                new FilmRating(2, "PG"),
                new FilmRating(3, "PG-13"),
                new FilmRating(4, "R"),
                new FilmRating(5, "NC-17")
        );
        when(filmRatingStorage.findAll()).thenReturn(ratings);
        when(filmRatingStorage.findById(1)).thenReturn(Optional.ofNullable(ratings.get(0)));
        when(filmRatingStorage.findById(2)).thenReturn(Optional.ofNullable(ratings.get(1)));
        when(filmRatingStorage.findById(3)).thenReturn(Optional.ofNullable(ratings.get(2)));
        when(filmRatingStorage.findById(4)).thenReturn(Optional.ofNullable(ratings.get(3)));
        when(filmRatingStorage.findById(5)).thenReturn(Optional.ofNullable(ratings.get(4)));
    }

    @Test
    void getAllFilmRatings_ShouldReturnAllRatingsWithRussianLocale() {
        List<FilmRatingDto> result = filmRatingService.getAllFilmRatings();

        assertThat(result).hasSize(5);

        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("G");

        assertThat(result.get(1).getId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("PG");

        assertThat(result.get(2).getId()).isEqualTo(3);
        assertThat(result.get(2).getName()).isEqualTo("PG-13");

        assertThat(result.get(3).getId()).isEqualTo(4);
        assertThat(result.get(3).getName()).isEqualTo("R");

        assertThat(result.get(4).getId()).isEqualTo(5);
        assertThat(result.get(4).getName()).isEqualTo("NC-17");
    }

    @Test
    void getAllFilmRatings_ShouldReturnRatingsInCorrectOrder() {
        List<FilmRatingDto> result = filmRatingService.getAllFilmRatings();

        assertThat(result)
                .extracting(FilmRatingDto::getId)
                .containsExactly(1, 2, 3, 4, 5);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void getFilmRatingById_WithValidId_ShouldReturnCorrectRating(int id) {
        FilmRatingDto result = filmRatingService.getFilmRatingById(id);

        assertThat(result.getId()).isEqualTo(id);

        switch (id) {
            case 1 -> assertThat(result.getName()).isEqualTo("G");
            case 2 -> assertThat(result.getName()).isEqualTo("PG");
            case 3 -> assertThat(result.getName()).isEqualTo("PG-13");
            case 4 -> assertThat(result.getName()).isEqualTo("R");
            case 5 -> assertThat(result.getName()).isEqualTo("NC-17");
        }
    }

    @Test
    void getFilmRatingById_WithId1_ShouldReturnGRating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("G");
    }

    @Test
    void getFilmRatingById_WithId2_ShouldReturnPGRating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(2);

        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getName()).isEqualTo("PG");
    }

    @Test
    void getFilmRatingById_WithId3_ShouldReturnPG13Rating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(3);

        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("PG-13");
    }

    @Test
    void getFilmRatingById_WithId4_ShouldReturnRRating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(4);

        assertThat(result.getId()).isEqualTo(4);
        assertThat(result.getName()).isEqualTo("R");
    }

    @Test
    void getFilmRatingById_WithId5_ShouldReturnNC17Rating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(5);

        assertThat(result.getId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("NC-17");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, 10, -1, 100})
    void getFilmRatingById_WithInvalidId_ShouldThrowException(int invalidId) {
        assertThatThrownBy(() -> filmRatingService.getFilmRatingById(invalidId))
                .isInstanceOf(NoFilmRatingFoundException.class)
                .hasMessage("No film rating found with id: " + invalidId);
    }

    @Test
    void getAllFilmRatings_ShouldReturnNewListEachTime() {
        List<FilmRatingDto> firstCall = filmRatingService.getAllFilmRatings();
        List<FilmRatingDto> secondCall = filmRatingService.getAllFilmRatings();

        assertThat(firstCall).isNotSameAs(secondCall);
        assertThat(firstCall).isEqualTo(secondCall);
    }

    @Test
    void getAllFilmRatings_ShouldNotReturnNull() {
        List<FilmRatingDto> result = filmRatingService.getAllFilmRatings();

        assertThat(result).isNotNull();
        assertThat(result).allSatisfy(dto -> {
            assertThat(dto.getId()).isNotNull();
            assertThat(dto.getName()).isNotNull();
        });
    }
}
