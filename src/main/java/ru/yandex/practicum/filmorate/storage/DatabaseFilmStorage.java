package ru.yandex.practicum.filmorate.storage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@Primary
@Repository("databaseFilmStorage")
public class DatabaseFilmStorage extends DatabaseStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String SAVE_FILM_QUERY = "INSERT INTO films (name, description, release_date, duration, rating) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating = ? WHERE id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE id = ?";
    private static final String FIND_LIKED_BY_FOR_FILM_QUERY = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private static final String LIKE_FILM_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String CLEAR_FILM_LIKES_QUERY = "DELETE FROM film_likes WHERE film_id = ?";
    private static final String UNLIKE_FILM_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_MOST_POPULAR_FILMS_QUERY = "SELECT f.*, COUNT(fl.user_id) as like_count FROM films f" +
            " LEFT JOIN film_likes fl ON f.id = fl.film_id GROUP BY f.id ORDER BY like_count DESC LIMIT ?";
    private static final String CLEAR_GENRES_FOR_FILM_QUERY = "DELETE FROM film_genres_mapper WHERE film_id = ?";

    @Autowired
    public DatabaseFilmStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Film> findFilmById(long id) {
        return findOne(FIND_FILM_BY_ID_QUERY, id);
    }

    @Override
    public Film save(Film film) {
        long id = insert(SAVE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating().getId()
        );
        film.setId(id);
        log.info("Saved new film with id {}", id);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new InvalidFilmDataException("Film id is null. Failed to update film");
        }
        update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? film.getReleaseDate() : null,
                film.getDuration(),
                film.getRating().getId(),
                film.getId()
        );

        log.info("Updated new film with id {}", film.getId());
        return film;
    }

    @Override
    public boolean delete(long id) {
        // clear film_genres_mapper table for this film
        jdbc.update(CLEAR_GENRES_FOR_FILM_QUERY, id);
        log.info("Delete film with id {}", id);
        return delete(DELETE_FILM_QUERY, id);
    }

    @Override
    public List<Film> findMostPopularFilms(Integer size) {
        log.debug("Find most popular films");
        return findMany(FIND_MOST_POPULAR_FILMS_QUERY, size);
    }

    @Override
    public Set<Long> findFilmLikedBy(Long filmId) {
        log.debug("Load film liked by users list for film {}", filmId);
        return new HashSet<>(jdbc.queryForList(FIND_LIKED_BY_FOR_FILM_QUERY, Long.class, filmId));
    }

    @Override
    public boolean likeFilm(Long filmId, Long userId) {
        jdbc.update(LIKE_FILM_QUERY, filmId, userId);
        return true;
    }

    @Override
    public boolean unlikeFilm(Long filmId, Long userId) {
        jdbc.update(UNLIKE_FILM_QUERY, filmId, userId);
        return true;
    }
}
