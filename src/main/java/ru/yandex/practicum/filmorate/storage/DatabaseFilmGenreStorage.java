package ru.yandex.practicum.filmorate.storage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.model.FilmGenre;

@Slf4j
@Repository("databaseFilmGenreStorage")
public class DatabaseFilmGenreStorage extends DatabaseStorage<FilmGenre> implements FilmGenreStorage {

    private static final String FIND_ALL_QUERY = "SELECT id, name FROM film_genre ORDER BY id";
    private static final String FIND_GENRE_BY_ID_QUERY = "SELECT id, name FROM film_genre WHERE id = ?";
    private static final String SAVE_GENRE_QUERY = "INSERT INTO film_genre (name) VALUES (?)";
    private static final String UPDATE_GENRE_QUERY = "UPDATE film_genre SET name = ? WHERE id = ?";
    private static final String DELETE_GENRE_QUERY = "DELETE FROM film_genre WHERE id = ?";

    private static final String FIND_GENRES_IDS_BY_FILM_ID_QUERY = "SELECT genre_id FROM film_genres_mapper WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY = "SELECT film_genre.* FROM film_genre LEFT JOIN film_genres_mapper ON film_genre.id = film_genres_mapper.film_id WHERE film_genres_mapper.film_id = ?";
    private static final String SAVE_GENRES_FOR_FILM_QUERY = "INSERT INTO film_genres_mapper (film_id, genre_id) VALUES (?, ?)";
    private static final String CLEAR_GENRES_FOR_FILM_QUERY = "DELETE FROM film_genres_mapper WHERE film_id = ?";

    public DatabaseFilmGenreStorage(JdbcTemplate jdbc, RowMapper<FilmGenre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<FilmGenre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<FilmGenre> findFilmGenreById(long id) {
        log.debug("Fild film genre by id {}", id);
        return findOne(FIND_GENRE_BY_ID_QUERY, id);
    }

    @Override
    public FilmGenre save(FilmGenre genre) {
        long id = insert(SAVE_GENRE_QUERY, genre.getName());
        genre.setId(id);
        log.info("Saved new genre with id: {}", id);
        return genre;
    }

    @Override
    public FilmGenre update(FilmGenre genre) {
        if (findFilmGenreById(genre.getId()).isEmpty()) {
            throw new InvalidFilmDataException("Film genre id is empty. Failed to update film genre");
        }
        update(UPDATE_GENRE_QUERY, genre.getName(), genre.getId());
        log.info("Updated genre with id: {}", genre.getId());
        return genre;
    }

    @Override
    public boolean delete(long id) {
        if (findFilmGenreById(id).isEmpty()) {
            throw new InvalidFilmDataException("Film genre id is empty. Failed to update film genre");
        }
        log.info("Delete genre with id: {}", id);
        return delete(DELETE_GENRE_QUERY, id);
    }

    @Override
    public Set<Long> findFilmGenresIdsByFilmId(Long filmId) {
        log.debug("Load film genres list for film {}", filmId);
        return new HashSet<>(jdbc.queryForList(FIND_GENRES_IDS_BY_FILM_ID_QUERY, Long.class, filmId));
    }

    @Override
    public Set<FilmGenre> findFilmGenresByFilmId(Long filmId) {
        log.debug("Load film genres list for film {}", filmId);

        return new HashSet<>(findMany(FIND_GENRES_BY_FILM_ID_QUERY, filmId));
    }

    @Override
    public void clearFilmGenresForFilm(Long filmId) {
        jdbc.update(CLEAR_GENRES_FOR_FILM_QUERY, filmId);
    }

    @Override
    public void saveFilmGenresForFilm(Long filmId, Set<Long> genres) {
        log.info("Update film {} genres list", filmId);
        clearFilmGenresForFilm(filmId);

        List<Object[]> filmGenrePair = genres.stream()
                .map(genre -> new Object[] {filmId, genre})
                .collect(Collectors.toList());

        jdbc.batchUpdate(SAVE_GENRES_FOR_FILM_QUERY, filmGenrePair);
    }
}
