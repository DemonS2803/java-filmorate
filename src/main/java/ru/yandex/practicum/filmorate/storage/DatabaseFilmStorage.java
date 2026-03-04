package ru.yandex.practicum.filmorate.storage;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

@Primary
@Repository("databaseFilmStorage")
public class DatabaseFilmStorage extends DatabaseStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String SAVE_FILM_QUERY = "INSERT INTO films (name, description, release_date, duration) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE id = ?";
    private static final String FIND_LIKED_BY_FOR_FILM_QUERY = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private static final String LIKE_FILM_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String CLEAR_FILM_LIKES_QUERY = "DELETE FROM film_likes WHERE film_id = ?";
    private static final String FIND_MOST_POPULAR_FILMS_QUERY = "SELECT f.*, COUNT(fl.user_id) as like_count FROM films f" +
            " LEFT JOIN film_likes fl ON f.id = fl.film_id GROUP BY f.id ORDER BY like_count DESC LIMIT ?";

    @Autowired
    public DatabaseFilmStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY).stream()
                .map(this::loadFilmLikes)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Film> findFilmById(long id) {
        return findOne(FIND_FILM_BY_ID_QUERY, id)
                .map(this::loadFilmLikes);
    }

    @Override
    public Film save(Film film) {
        long id = insert(SAVE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );
        film.setId(id);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? new Date(film.getReleaseDate().getTime()) : null,
                film.getDuration(),
                film.getId()
        );

        updateFilmLikes(film.getId(), film.getLikedByUsers());

        return film;
    }

    @Override
    public boolean delete(long id) {
        return delete(DELETE_FILM_QUERY, id);
    }

    @Override
    public List<Film> findMostPopularFilms(Integer size) {
        return findMany(FIND_MOST_POPULAR_FILMS_QUERY, size).stream()
                .map(this::loadFilmLikes)
                .collect(Collectors.toList());
    }

    private Film loadFilmLikes(Film film) {
        Set<Long> likes = new HashSet<>(jdbc.queryForList(FIND_LIKED_BY_FOR_FILM_QUERY, Long.class, film.getId()));
        film.setLikedByUsers(likes);
        return film;
    }

    private void saveFilmLikes(Long filmId, Set<Long> likedByUserIds) {
        if (likedByUserIds == null || likedByUserIds.isEmpty()) {
            return;
        }

        for (Long userId : likedByUserIds) {
            jdbc.update(LIKE_FILM_QUERY, filmId, userId);
        }
    }

    private void updateFilmLikes(Long filmId, Set<Long> likedByUserIds) {
        jdbc.update(CLEAR_FILM_LIKES_QUERY, filmId);

        if (likedByUserIds != null && !likedByUserIds.isEmpty()) {
            saveFilmLikes(filmId, likedByUserIds);
        }
    }
}
