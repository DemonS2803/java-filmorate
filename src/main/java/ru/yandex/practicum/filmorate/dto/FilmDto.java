package ru.yandex.practicum.filmorate.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilmDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Long> likedByUsers;
    private FilmRatingDto mpa;
    private List<FilmGenreDto> genres;

}
