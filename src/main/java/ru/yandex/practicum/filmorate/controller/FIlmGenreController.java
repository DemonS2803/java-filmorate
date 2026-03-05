package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.service.FilmGenreService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class FIlmGenreController {

    private final FilmGenreService filmGenreService;

    @GetMapping
    public List<FilmGenreDto> getFilmGenres() {
        return filmGenreService.getAllFilmGenres();
    }

    @GetMapping("/{id}")
    public FilmGenreDto getFilmGenreById(@PathVariable("id") int id) {
        return filmGenreService.getFilmGenreById(id);
    }

}
