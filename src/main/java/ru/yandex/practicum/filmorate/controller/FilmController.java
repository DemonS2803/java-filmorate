package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exceptions.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private Long filmCurrentId = 1L;

    @GetMapping
    public Collection<Film> getFilms() {
        log.debug("Get all films info");
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody FilmDto incomingFilmDto) {
        Film film = Film.of(incomingFilmDto);
        if (film.getId() == null) {
            log.info("Film id is empty. Set new id: {}", filmCurrentId);
            film.setId(filmCurrentId++);
        }
        if (films.containsKey(film.getId())) {
            log.warn("Film with id {} already exists. Set new id: {}", film.getId(), filmCurrentId);
            film.setId(filmCurrentId++);
        }
        log.info("User added new film: {}", film);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody FilmDto incomingFilmDto) {
        log.info("User want update film: {}", incomingFilmDto);
        Film film = Film.of(incomingFilmDto);
        if (film.getId() == null) {
            throw new InvalidFilmDataException("Film id is empty. Failed to update film");
        }
        if (!films.containsKey(film.getId())) {
            throw new NoFilmFoundException("Film with id " + film.getId() + " does not exist");
        }

        log.info("User updated film with id {}", film.getId());
        films.put(film.getId(), film);
        return film;
    }

}
