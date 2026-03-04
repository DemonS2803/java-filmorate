package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

public class FilmMapper {

    public static Film mapToFilm(FilmDto dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        if (dto.getLikedByUsers() != null && !dto.getLikedByUsers().isEmpty()) {
            film.getLikedByUsers().addAll(dto.getLikedByUsers());
        }
        return film;
    }

}
