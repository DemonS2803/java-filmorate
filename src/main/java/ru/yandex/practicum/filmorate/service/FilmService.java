package ru.yandex.practicum.filmorate.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.debug("Get all films");
        return filmStorage.findAll();
    }

    public Film getFilmById(final long id) {
        log.debug("Get film by id: {}", id);
        return filmStorage.findFilmByIdOrThrow(id);
    }

    public Film createFilm(Film film) {
        log.info("Create film: {}", film);
        return filmStorage.save(film);
    }

    public Film updateFilm(Film film) {
        // check for film exists
        getFilmById(film.getId());

        log.info("Update film: {}", film);
        return filmStorage.update(film);
    }

    public Film likeFilm(Long userId, Long filmId) {
        log.info("Like film: {}", filmId);
        Film film = getFilmById(filmId);
        // check for user exists
        userStorage.findUserByIdOrThrow(userId);

        film.getLikedByUsers().add(userId);
        film = filmStorage.update(film);

        return film;
    }

    public Film unlikeFilm(Long userId, Long filmId) {
        log.info("Unlike film: {}", filmId);
        Film film = getFilmById(filmId);
        // check for user exists
        userStorage.findUserByIdOrThrow(userId);

        film.getLikedByUsers().remove(userId);
        film = filmStorage.update(film);

        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        log.debug("Get popular films");
        if (count == null || count <= 0) {
            log.error("Get popular films: count must be greater than 0");
            return new ArrayList<>();
        }
        return filmStorage.findMostPopularFilms(count);
    }

}
