package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DatabaseFilmGenreStorageTest {

    @Autowired
    private final DatabaseFilmGenreStorage genreStorage;

    private FilmGenre testGenre;
    private FilmGenre testGenre2;

    @BeforeEach
    void setUp() {
        testGenre = new FilmGenre();
        testGenre.setName("Test Comedy");

        testGenre2 = new FilmGenre();
        testGenre2.setName("Test Drama");
    }

    @Test
    void findAll_ShouldReturnAllGenres() {
        genreStorage.save(testGenre);
        genreStorage.save(testGenre2);

        List<FilmGenre> genres = genreStorage.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres.size()).isGreaterThanOrEqualTo(2);
        assertThat(genres).extracting(FilmGenre::getName)
                .contains(testGenre.getName(), testGenre2.getName());
    }

    @Test
    void save_ShouldSaveAndReturnGenreWithId() {
        FilmGenre savedGenre = genreStorage.save(testGenre);

        assertThat(savedGenre.getId()).isNotNull();
        assertThat(savedGenre.getName()).isEqualTo(testGenre.getName());

        Optional<FilmGenre> found = genreStorage.findFilmGenreById(savedGenre.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(testGenre.getName());
    }

    @Test
    void findFilmGenreById_WithExistingId_ShouldReturnGenre() {
        FilmGenre savedGenre = genreStorage.save(testGenre);

        Optional<FilmGenre> found = genreStorage.findFilmGenreById(savedGenre.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedGenre.getId());
        assertThat(found.get().getName()).isEqualTo(testGenre.getName());
    }

    @Test
    void findFilmGenreById_WithNonExistingId_ShouldReturnEmptyOptional() {
        Optional<FilmGenre> found = genreStorage.findFilmGenreById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void update_ShouldUpdateExistingGenre() {
        FilmGenre savedGenre = genreStorage.save(testGenre);
        String updatedName = "Updated Genre Name";
        savedGenre.setName(updatedName);

        FilmGenre updated = genreStorage.update(savedGenre);

        assertThat(updated.getName()).isEqualTo(updatedName);

        Optional<FilmGenre> found = genreStorage.findFilmGenreById(savedGenre.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(updatedName);
    }

    @Test
    void delete_WithExistingId_ShouldReturnTrueAndRemoveGenre() {
        FilmGenre savedGenre = genreStorage.save(testGenre);
        Long id = savedGenre.getId();

        boolean deleted = genreStorage.delete(id);

        assertThat(deleted).isTrue();
        assertThat(genreStorage.findFilmGenreById(id)).isEmpty();
    }

    @Test
    void save_WithNullName_ShouldThrowException() {
        FilmGenre invalidGenre = new FilmGenre();
        invalidGenre.setName(null);

        assertThatThrownBy(() -> genreStorage.save(invalidGenre))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void crudOperations_ShouldWorkInSequence() {
        FilmGenre created = genreStorage.save(testGenre);
        assertThat(created.getId()).isNotNull();
        long id = created.getId();

        Optional<FilmGenre> found = genreStorage.findFilmGenreById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(testGenre.getName());

        found.get().setName("Updated CRUD Name");
        FilmGenre updated = genreStorage.update(found.get());
        assertThat(updated.getName()).isEqualTo("Updated CRUD Name");

        Optional<FilmGenre> foundAfterUpdate = genreStorage.findFilmGenreById(id);
        assertThat(foundAfterUpdate).isPresent();
        assertThat(foundAfterUpdate.get().getName()).isEqualTo("Updated CRUD Name");

        boolean deleted = genreStorage.delete(id);
        assertThat(deleted).isTrue();

        Optional<FilmGenre> foundAfterDelete = genreStorage.findFilmGenreById(id);
        assertThat(foundAfterDelete).isEmpty();
    }

    @Test
    void findAll_ShouldIncludeDefaultGenres() {
        List<FilmGenre> genres = genreStorage.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres).extracting(FilmGenre::getName)
                .contains("Комедия", "Драма", "Боевик");
    }

}
