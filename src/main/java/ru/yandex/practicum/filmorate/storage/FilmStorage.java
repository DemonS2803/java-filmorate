package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {

    List<Film> findAll();

    Optional<Film> findFilmById(long id);

    Film save(Film film);

    Film update(Film film);

    boolean delete(long id);

    List<Film> findMostPopularFilms(Integer size);

    boolean likeFilm(Long filmId, Long userId);

    boolean unlikeFilm(Long filmId, Long userId);

    Set<Long> findFilmLikedBy(Long filmId);

}
