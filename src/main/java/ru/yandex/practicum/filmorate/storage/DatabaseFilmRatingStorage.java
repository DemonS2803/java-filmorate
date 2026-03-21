package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmRating;

@Repository("databaseFilmRatingStorage")
public class DatabaseFilmRatingStorage extends DatabaseStorage<FilmRating> implements FilmRatingStorage {

    public static final String FIND_ALL_QUERY = "SELECT * FROM film_ratings";
    public static final String FIND_FILM_RATING_BY_ID_QUERY = "SELECT * FROM film_ratings WHERE id = ?";

    public DatabaseFilmRatingStorage(JdbcTemplate jdbc, RowMapper<FilmRating> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<FilmRating> findById(int id) {
        return findOne(FIND_FILM_RATING_BY_ID_QUERY, id);
    }

    @Override
    public List<FilmRating> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

}
