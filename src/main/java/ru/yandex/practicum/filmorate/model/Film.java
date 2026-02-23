package ru.yandex.practicum.filmorate.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.dto.FilmDto;

/**
 * Film.
 */
@Getter
@Setter
@ToString
public class Film {

    Long id;
    String name;
    String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date releaseDate;
    Integer duration;
    Set<Long> likedByUsers = new HashSet<>();

    public static Film of(FilmDto dto) {
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
