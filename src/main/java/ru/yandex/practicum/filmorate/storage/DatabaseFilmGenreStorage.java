package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

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

    public DatabaseFilmGenreStorage(JdbcTemplate jdbc, RowMapper<FilmGenre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<FilmGenre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<FilmGenre> findFilmGenreById(long id) {
        return findOne(FIND_GENRE_BY_ID_QUERY, id);
    }

    @Override
    public FilmGenre save(FilmGenre genre) {
        long id = insert(SAVE_GENRE_QUERY, genre.getName());
        genre.setId(id);
        return genre;
    }

    @Override
    public FilmGenre update(FilmGenre genre) {
        if (findFilmGenreById(genre.getId()).isEmpty()) {
            throw new InvalidFilmDataException("Film genre id is empty. Failed to update film genre");
        }
        update(UPDATE_GENRE_QUERY, genre.getName(), genre.getId());
        return genre;
    }

    @Override
    public boolean delete(long id) {
        if (findFilmGenreById(id).isEmpty()) {
            throw new InvalidFilmDataException("Film genre id is empty. Failed to update film genre");
        }
        return delete(DELETE_GENRE_QUERY, id);
    }
}
