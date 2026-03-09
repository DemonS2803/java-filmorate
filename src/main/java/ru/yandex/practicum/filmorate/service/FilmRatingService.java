package ru.yandex.practicum.filmorate.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRatingMapper;
import ru.yandex.practicum.filmorate.model.FilmRating;

@Slf4j
@Service
public class FilmRatingService {

    public static final String FILM_RATING_LOCALE = "en";

    public List<FilmRatingDto> getAllFilmRatings() {
        log.debug("Get all film ratings");
        return Arrays.stream(FilmRating.values())
                .filter(Objects::nonNull)
                .map(rating -> FilmRatingMapper.toDto(rating, FILM_RATING_LOCALE))
                .toList();
    }

    public FilmRatingDto getFilmRatingById(final int id) {
        log.debug("Get film rating by id: {}", id);

        FilmRating rating = FilmRating.valueOf(id);
        // check id > 5 analogue
        if (rating == null) {
            log.error("Invalid film rating id: {}", id);
            throw new NoFilmRatingFoundException("No film rating with id " + id);
        }
        return FilmRatingMapper.toDto(rating, FILM_RATING_LOCALE);
    }

}
