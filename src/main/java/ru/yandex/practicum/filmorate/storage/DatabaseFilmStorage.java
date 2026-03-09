package ru.yandex.practicum.filmorate.storage;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String FIND_MOST_POPULAR_FILMS_QUERY = "SELECT f.*, COUNT(fl.user_id) as like_count FROM films f" +
            " LEFT JOIN film_likes fl ON f.id = fl.film_id GROUP BY f.id ORDER BY like_count DESC LIMIT ?";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY = "SELECT genre_id FROM film_genres_mapper WHERE film_id = ?";
    private static final String SAVE_GENRES_FOR_FILM_QUERY = "INSERT INTO film_genres_mapper (film_id, genre_id) VALUES (?, ?)";
    private static final String CLEAR_GENRES_FOR_FILM_QUERY = "DELETE FROM film_genres_mapper WHERE film_id = ?";

    @Autowired
    public DatabaseFilmStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY).stream()
                .map(this::loadAdditionalFields)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Film> findFilmById(long id) {
        return findOne(FIND_FILM_BY_ID_QUERY, id)
                .map(this::loadAdditionalFields);
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
        saveFilmGenres(id, film.getGenres());
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
                film.getReleaseDate() != null ? new Date(film.getReleaseDate().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()) : null,
                film.getDuration(),
                film.getRating().getId(),
                film.getId()
        );
        updateFilmLikes(film.getId(), film.getLikedByUsers());
        updateFilmGenres(film.getId(), film.getGenres());

        log.info("Updated new film with id {}", film.getId());
        return film;
    }

    @Override
    public boolean delete(long id) {
        // clear film_genres_mapper table for this film
        updateFilmGenres(id, new HashSet<>());
        log.info("Delete film with id {}", id);
        return delete(DELETE_FILM_QUERY, id);
    }

    @Override
    public List<Film> findMostPopularFilms(Integer size) {
        log.debug("Find most popular films");
        return findMany(FIND_MOST_POPULAR_FILMS_QUERY, size).stream()
                .map(this::loadAdditionalFields)
                .collect(Collectors.toList());
    }

    private Film loadAdditionalFields(Film film) {
        log.debug("Load additional field for film {}", film.getId());
        film = loadFilmLikes(film);
        film = loadFilmGenres(film);
        return film;
    }

    private Film loadFilmLikes(Film film) {
        log.debug("Load film liked by users list for film {}", film.getId());
        Set<Long> likes = new HashSet<>(jdbc.queryForList(FIND_LIKED_BY_FOR_FILM_QUERY, Long.class, film.getId()));
        film.setLikedByUsers(likes);
        return film;
    }


    private Film loadFilmGenres(Film film) {
        log.debug("Load film genres list for film {}", film.getId());
        Set<Long> genres = new HashSet<>(jdbc.queryForList(FIND_GENRES_BY_FILM_ID_QUERY, Long.class, film.getId()));
        film.setGenres(genres);
        return film;
    }

    private void saveFilmLikes(Long filmId, Set<Long> likedByUserIds) {
        log.info("Save film {} likes list", filmId);
        if (likedByUserIds == null || likedByUserIds.isEmpty()) {
            return;
        }

        for (Long userId : likedByUserIds) {
            jdbc.update(LIKE_FILM_QUERY, filmId, userId);
        }
    }

    private void updateFilmLikes(Long filmId, Set<Long> likedByUserIds) {
        log.info("Update film {} likes list", filmId);
        jdbc.update(CLEAR_FILM_LIKES_QUERY, filmId);

        if (likedByUserIds != null && !likedByUserIds.isEmpty()) {
            saveFilmLikes(filmId, likedByUserIds);
        }
    }

    private void saveFilmGenres(Long filmId, Set<Long> genres) {
        log.info("Save film {} genres list", filmId);
        if (genres == null || genres.isEmpty()) {
            return;
        }

        for (Long genreId : genres) {
            jdbc.update(SAVE_GENRES_FOR_FILM_QUERY, filmId, genreId);
        }
    }

    private void updateFilmGenres(Long filmId, Set<Long> genres) {
        log.info("Update film {} genres list", filmId);
        jdbc.update(CLEAR_GENRES_FOR_FILM_QUERY, filmId);

        if (genres != null && !genres.isEmpty()) {
            saveFilmGenres(filmId, genres);
        }
    }

}
