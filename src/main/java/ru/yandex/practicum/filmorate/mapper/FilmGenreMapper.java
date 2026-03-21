package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.model.FilmGenre;

public class FilmGenreMapper {

    public static FilmGenreDto toDto(final FilmGenre filmGenre) {
        FilmGenreDto dto = new FilmGenreDto();
        dto.setId(filmGenre.getId());
        dto.setName(filmGenre.getName());
        return dto;
    }
}
