package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRatingMapper;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmRatingStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmRatingService {

    private final FilmRatingStorage filmRatingStorage;

    public List<FilmRatingDto> getAllFilmRatings() {
        log.debug("Get all film ratings");
        return filmRatingStorage.findAll().stream()
                .filter(Objects::nonNull)
                .map(FilmRatingMapper::toDto)
                .toList();
    }

    public FilmRatingDto getFilmRatingById(final int id) {
        log.debug("Get film rating by id: {}", id);


        return FilmRatingMapper.toDto(getFilmRatingByIdOrThrow(id));
    }

    private FilmRating getFilmRatingByIdOrThrow(Integer id) {
        return filmRatingStorage.findById(id)
                .orElseThrow(() -> new NoFilmRatingFoundException("No film rating found with id: " + id));
    }

}
