package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;
import java.util.Optional;

public interface FilmGenreStorage {

    List<FilmGenre> findAll();

    Optional<FilmGenre> findFilmGenreById(long id);

    FilmGenre save(FilmGenre film);

    FilmGenre update(FilmGenre film);

    boolean delete(long id);

}
