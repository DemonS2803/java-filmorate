package ru.yandex.practicum.filmorate.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmGenreFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmGenreService {
    @Qualifier("databaseFilmGenreStorage")
    private final FilmGenreStorage filmGenreStorage;

    public List<FilmGenreDto> getAllFilmGenres() {
        log.debug("Get all film genres");
        return filmGenreStorage.findAll().stream()
                .map(FilmGenreMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<FilmGenreDto> getFilmGenresByIds(Set<Long> ids) {
        log.debug("Get film genres by ids: {}", ids);
        List<FilmGenreDto> dtos = ids.stream().map(this::getFilmGenreByIdOrThrow)
                .sorted(Comparator.comparing(FilmGenreDto::getId))
                .collect(Collectors.toList());
        return dtos;
    }

    public FilmGenreDto getFilmGenreById(long id) {
        return getFilmGenreByIdOrThrow(id);
    }

    private FilmGenreDto getFilmGenreByIdOrThrow(long id) {
        log.debug("Get film genre by id: {}", id);
        return filmGenreStorage.findFilmGenreById(id)
                .map(FilmGenreMapper::toDto)
                .orElseThrow(() -> new NoFilmGenreFoundException("No film genre with id " + id + " was found"));
    }

}
