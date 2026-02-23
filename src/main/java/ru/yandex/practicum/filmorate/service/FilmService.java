package ru.yandex.practicum.filmorate.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.NoFilmFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film getFilmById(final long id) {
        return filmStorage.findFilmById(id)
                .orElseThrow(() -> new NoFilmFoundException("No film with id " + id + " found"));
    }

    public Film createFilm(Film film) {
        return filmStorage.save(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.update(film);
    }

    public Film likeFilm(Long userId, Long filmId) {
        Film film = getFilmById(filmId);
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NoUserFoundException("No user with id " + userId + " found"));

        film.getLikedByUsers().add(userId);
        film = filmStorage.update(film);

        return film;
    }

    public Film unlikeFilm(Long userId, Long filmId) {
        Film film = getFilmById(filmId);
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NoUserFoundException("No user with id " + userId + " found"));

        film.getLikedByUsers().remove(userId);
        film = filmStorage.update(film);

        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.findMostPopularFilms(count);
    }

}
