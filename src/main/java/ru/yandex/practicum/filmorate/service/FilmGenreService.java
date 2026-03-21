package ru.yandex.practicum.filmorate.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmGenreFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.model.FilmGenre;
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

    public List<FilmGenreDto> getFilmGenresByIds(List<Long> ids) {
        log.debug("Get film genres by ids: {}", ids);
        return filmGenreStorage.findFilmGenresByIds(ids).stream()
                .map(FilmGenreMapper::toDto)
                .sorted(Comparator.comparing(FilmGenreDto::getId))
                .collect(Collectors.toList());
    }

    public boolean isAllFilmGenresExists(List<Long> ids) {
        log.debug("Check if all film genres exists: {}", ids);
        Set<Long> existingIds = filmGenreStorage.findFilmGenresByIds(ids).stream()
                .map(FilmGenre::getId)
                .collect(Collectors.toSet());
        boolean hasAnyMissing = ids.stream()
                .anyMatch(id -> !existingIds.contains(id));

        return !hasAnyMissing;
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

    public List<Long> getFilmGenresIdsByFilmId(Long filmId) {
        return filmGenreStorage.findFilmGenresIdsByFilmId(filmId);
    }

    public List<FilmGenreDto> getFilmGenresByFilmId(Long filmId) {
        return filmGenreStorage.findFilmGenresByFilmId(filmId).stream()
                .map(FilmGenreMapper::toDto)
                .sorted(Comparator.comparing(FilmGenreDto::getId))
                .collect(Collectors.toList());
    }

    public void saveFilmGenresForFilm(Long filmId, List<Long> genresIds) {
        filmGenreStorage.saveFilmGenresForFilm(filmId, new HashSet<>(genresIds));
    }

    public Map<Long, Set<FilmGenre>> getFilmGenresByFilmsIds(List<Long> filmsIds) {
        return filmGenreStorage.findFilmGenresForFilms(filmsIds);
    }

}
