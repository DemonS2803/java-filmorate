package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmGenreFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmGenreService {
    @Qualifier("databaseFilmGenreStorage")
    private final FilmGenreStorage filmGenreStorage;

    public List<FilmGenreDto> getAllFilmGenres() {
        return filmGenreStorage.findAll().stream()
                .map(FilmGenreMapper::toDto)
                .collect(Collectors.toList());
    }

    public FilmGenreDto getFilmGenreById(final int id) {
        return getFilmGenreByIdOrThrow(id);
    }

    private FilmGenreDto getFilmGenreByIdOrThrow(long id) {
        return filmGenreStorage.findFilmGenreById(id)
                .map(FilmGenreMapper::toDto)
                .orElseThrow(() -> new NoFilmGenreFoundException("No film genre with id " + id + " was found"));
    }

}
