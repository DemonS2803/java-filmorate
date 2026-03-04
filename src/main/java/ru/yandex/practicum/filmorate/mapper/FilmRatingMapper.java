package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.model.FilmRating;

public class FilmRatingMapper {

    // хотелось сделать через Locale класс, но что-то он не подтянулся
    public static FilmRatingDto toDto(final FilmRating filmRating, String locale) {
        FilmRatingDto dto = new FilmRatingDto();
        dto.setId(FilmRating.getIdFor(filmRating));
        switch (locale) {
            case "ru":
                dto.setName(filmRating.getRu());
                break;
            default:
                dto.setName(filmRating.getUs());
        }
        return dto;
    }

}
