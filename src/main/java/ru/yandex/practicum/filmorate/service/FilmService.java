package ru.yandex.practicum.filmorate.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    public List<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        return filmStorage.save(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.update(film);
    }

}
