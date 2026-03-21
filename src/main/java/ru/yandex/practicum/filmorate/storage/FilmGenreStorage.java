package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmGenreStorage {

    List<FilmGenre> findAll();

    Optional<FilmGenre> findFilmGenreById(long id);

    FilmGenre save(FilmGenre genre);

    FilmGenre update(FilmGenre genre);

    boolean delete(long id);

    Set<Long> findFilmGenresIdsByFilmId(Long filmId);

    Set<FilmGenre> findFilmGenresByFilmId(Long filmId);

    void saveFilmGenresForFilm(Long film, Set<Long> genres);

    void clearFilmGenresForFilm(Long filmId);

}
