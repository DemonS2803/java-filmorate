package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.model.FilmRating;

public class FilmRatingMapper {

    public static FilmRatingDto toDto(final FilmRating filmRating) {
        FilmRatingDto dto = new FilmRatingDto();
        dto.setId(filmRating.getId());
        dto.setName(filmRating.getName());

        return dto;
    }

}
