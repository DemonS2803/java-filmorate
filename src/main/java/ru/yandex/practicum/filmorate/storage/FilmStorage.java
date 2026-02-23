package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {

    List<Film> findAll();

    Optional<Film> findFilmById(long id);

    Film save(Film film);

    Film update(Film film);

    boolean delete(long id);

}
