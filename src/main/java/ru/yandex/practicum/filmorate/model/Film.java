package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    LocalDate releaseDate;
    Integer duration;
    Set<Long> likedByUsers = new HashSet<>();
    FilmRating rating;
    List<Long> genres;

}
