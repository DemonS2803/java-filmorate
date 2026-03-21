package ru.yandex.practicum.filmorate.storage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Film;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DatabaseFilmStorageTest {

    private final DatabaseFilmStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(120, 1, 1)); // 2020-01-01
        testFilm.setDuration(120);
        testFilm.setLikedByUsers(new HashSet<>());
        testFilm.setRating(2);
        testFilm.setGenres(Set.of(1L, 3L));
    }

    @Test
    void findAll_ShouldReturnAllFilms() {
        filmStorage.save(testFilm);

        List<Film> films = filmStorage.findAll();

        assertThat(films).isNotEmpty();
        assertThat(films.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findFilmById_WithExistingId_ShouldReturnFilm() {
        Film savedFilm = filmStorage.save(testFilm);

        Optional<Film> foundFilm = filmStorage.findFilmById(savedFilm.getId());

        assertTrue(foundFilm.isPresent());
        assertEquals(savedFilm.getName(), foundFilm.get().getName());
        assertEquals(savedFilm.getDescription(), foundFilm.get().getDescription());
    }

    @Test
    void findFilmById_WithNonExistingId_ShouldReturnEmpty() {
        Optional<Film> foundFilm = filmStorage.findFilmById(999L);

        assertFalse(foundFilm.isPresent());
    }

    @Test
    void save_ShouldCreateNewFilm() {
        Film savedFilm = filmStorage.save(testFilm);

        assertNotNull(savedFilm.getId());
        assertEquals(testFilm.getName(), savedFilm.getName());
        assertEquals(testFilm.getDuration(), savedFilm.getDuration());
    }

    @Test
    void save_WithLikes_ShouldSaveLikes() {
        Set<Long> likes = new HashSet<>();
        likes.add(1L);
        likes.add(2L);
        testFilm.setLikedByUsers(likes);

        Film savedFilm = filmStorage.save(testFilm);

        assertNotNull(savedFilm.getId());
        assertThat(savedFilm.getLikedByUsers()).hasSize(2);
    }

    @Test
    void update_ShouldModifyExistingFilm() {
        Film savedFilm = filmStorage.save(testFilm);
        savedFilm.setName("Updated Film");
        savedFilm.setDescription("Updated Description");
        savedFilm.setDuration(150);

        Film updatedFilm = filmStorage.update(savedFilm);

        assertEquals("Updated Film", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
        assertEquals(150, updatedFilm.getDuration());

        Optional<Film> foundFilm = filmStorage.findFilmById(savedFilm.getId());
        assertTrue(foundFilm.isPresent());
        assertEquals("Updated Film", foundFilm.get().getName());
    }

    @Test
    void update_WithLikes_ShouldUpdateLikes() {
        Film savedFilm = filmStorage.save(testFilm);

        Set<Long> likes = new HashSet<>();
        likes.add(1L);
        likes.add(2L);
        savedFilm.setLikedByUsers(likes);

        Film updatedFilm = filmStorage.update(savedFilm);

        assertThat(updatedFilm.getLikedByUsers()).hasSize(2);
    }

    @Test
    void delete_WithExistingId_ShouldReturnTrue() {
        Film savedFilm = filmStorage.save(testFilm);

        boolean deleted = filmStorage.delete(savedFilm.getId());

        assertTrue(deleted);
        Optional<Film> foundFilm = filmStorage.findFilmById(savedFilm.getId());
        assertFalse(foundFilm.isPresent());
    }

    @Test
    void delete_WithNonExistingId_ShouldReturnFalse() {
        boolean deleted = filmStorage.delete(999L);

        assertFalse(deleted);
    }

    @Test
    void findMostPopularFilms_WithDefaultSize_ShouldReturn10Films() {
        for (int i = 0; i < 15; i++) {
            Film film = new Film();
            film.setName("Film " + i);
            film.setDescription("Description " + i);
            film.setReleaseDate(LocalDate.now());
            film.setDuration(100 + i);
            film.setRating(2);
            film.setGenres(new HashSet<>());
            filmStorage.save(film);
        }

        List<Film> popularFilms = filmStorage.findMostPopularFilms(10);

        assertThat(popularFilms).hasSize(10);
    }

}
