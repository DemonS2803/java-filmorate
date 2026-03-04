package ru.yandex.practicum.filmorate.controller;

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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class FilmRatingController {

    private final FilmRatingService filmRatingService;

    @GetMapping
    public ResponseEntity<List<FilmRatingDto>> getFilmRatings() {
        return new ResponseEntity<>(filmRatingService.getAllFilmRatings(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmRatingDto> getFilmRatingById(@PathVariable("id") int id) {
        return new ResponseEntity<>(filmRatingService.getFilmRatingById(id), HttpStatus.OK);
    }

}
