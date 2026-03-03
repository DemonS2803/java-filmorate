package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        log.debug("Get all films info");
        return filmService.getFilms();
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody FilmDto incomingFilmDto) {
        log.info("Create film: {}", incomingFilmDto);
        Film film = filmService.createFilm(Film.of(incomingFilmDto));

        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody FilmDto incomingFilmDto) {
        log.info("Update film: {}", incomingFilmDto);
        Film film = filmService.updateFilm(Film.of(incomingFilmDto));

        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} like film {}", userId, id);
        Film film = filmService.likeFilm(userId, id);

        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> unlikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} unlike film {}", userId, id);
        Film film = filmService.unlikeFilm(userId, id);

        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopularFilms(
            @RequestParam(value = "count", defaultValue = "10") Integer count) {
        log.debug("Get {} popular films", count);
        return new ResponseEntity<>(filmService.getPopularFilms(count), HttpStatus.OK);
    }

}
