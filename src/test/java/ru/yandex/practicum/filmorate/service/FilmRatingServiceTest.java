package ru.yandex.practicum.filmorate.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@ExtendWith(MockitoExtension.class)
class FilmRatingServiceTest {

    private FilmRatingService filmRatingService;

    @BeforeEach
    void setUp() {
        filmRatingService = new FilmRatingService();
    }

    @Test
    void getAllFilmRatings_ShouldReturnAllRatingsWithRussianLocale() {
        List<FilmRatingDto> result = filmRatingService.getAllFilmRatings();

        assertThat(result).hasSize(5);

        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("0+");

        assertThat(result.get(1).getId()).isEqualTo(2);
        assertThat(result.get(1).getName()).isEqualTo("6+");

        assertThat(result.get(2).getId()).isEqualTo(3);
        assertThat(result.get(2).getName()).isEqualTo("12+");

        assertThat(result.get(3).getId()).isEqualTo(4);
        assertThat(result.get(3).getName()).isEqualTo("16+");

        assertThat(result.get(4).getId()).isEqualTo(5);
        assertThat(result.get(4).getName()).isEqualTo("18+");
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
            case 1 -> assertThat(result.getName()).isEqualTo("0+");
            case 2 -> assertThat(result.getName()).isEqualTo("6+");
            case 3 -> assertThat(result.getName()).isEqualTo("12+");
            case 4 -> assertThat(result.getName()).isEqualTo("16+");
            case 5 -> assertThat(result.getName()).isEqualTo("18+");
        }
    }

    @Test
    void getFilmRatingById_WithId1_ShouldReturnGRating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("0+");
    }

    @Test
    void getFilmRatingById_WithId2_ShouldReturnPGRating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(2);

        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getName()).isEqualTo("6+");
    }

    @Test
    void getFilmRatingById_WithId3_ShouldReturnPG13Rating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(3);

        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("12+");
    }

    @Test
    void getFilmRatingById_WithId4_ShouldReturnRRating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(4);

        assertThat(result.getId()).isEqualTo(4);
        assertThat(result.getName()).isEqualTo("16+");
    }

    @Test
    void getFilmRatingById_WithId5_ShouldReturnNC17Rating() {
        FilmRatingDto result = filmRatingService.getFilmRatingById(5);

        assertThat(result.getId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("18+");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, 10, -1, 100})
    void getFilmRatingById_WithInvalidId_ShouldThrowException(int invalidId) {
        assertThatThrownBy(() -> filmRatingService.getFilmRatingById(invalidId))
                .isInstanceOf(NoFilmRatingFoundException.class)
                .hasMessage("No film rating with id " + invalidId);
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
