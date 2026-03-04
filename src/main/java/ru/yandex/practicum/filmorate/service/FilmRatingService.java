package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRatingMapper;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FilmRatingService {

    public static final String FILM_RATING_LOCALE = "ru";

    public List<FilmRatingDto> getAllFilmRatings() {
        log.debug("Get all film ratings");
        return Arrays.stream(FilmRating.values())
                .map(rating -> FilmRatingMapper.toDto(rating, FILM_RATING_LOCALE))
                .toList();
    }

    public FilmRatingDto getFilmRatingById(final int id) {
        log.debug("Get film rating by id: {}", id);

        // check id > 5 analogue
        if (FilmRating.valueOf(id) == null) {
            log.error("Invalid film rating id: {}", id);
            throw new NoFilmRatingFoundException("No film rating with id " + id);
        }
        FilmRating rating = FilmRating.valueOf(id);
        return FilmRatingMapper.toDto(rating, FILM_RATING_LOCALE);
    }

}
