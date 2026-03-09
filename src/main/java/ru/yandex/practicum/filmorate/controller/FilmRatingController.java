package ru.yandex.practicum.filmorate.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.service.FilmRatingService;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class FilmRatingController {

    private final FilmRatingService filmRatingService;

    @GetMapping
    public ResponseEntity<List<FilmRatingDto>> getFilmRatings() {
        log.debug("Get all film ratings");
        List<FilmRatingDto> filmRatings = filmRatingService.getAllFilmRatings();
        return new ResponseEntity<>(filmRatings, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmRatingDto> getFilmRatingById(@PathVariable("id") int id) {
        log.debug("Get film rating by id {}", id);
        FilmRatingDto filmRating = filmRatingService.getFilmRatingById(id);
        return new ResponseEntity<>(filmRating, HttpStatus.OK);
    }

}
