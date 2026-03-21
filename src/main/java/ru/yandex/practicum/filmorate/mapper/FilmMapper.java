package ru.yandex.practicum.filmorate.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequestDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequestDto;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;

public class FilmMapper {

    public static Film mapToFilm(UpdateFilmRequestDto dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        film.setLikedByUsers(new HashSet<>());
        film.setGenres(new ArrayList<>());

        if (dto.getLikedByUsers() != null && !dto.getLikedByUsers().isEmpty()) {
            film.getLikedByUsers().addAll(dto.getLikedByUsers());
        }
        if (dto.getMpa() != null) {
            film.setRating(new FilmRating(dto.getMpa().getId(), null));
        }
        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            film.setGenres(dto.getGenres().stream().map(FilmGenreDto::getId).collect(Collectors.toList()));
        }
        return film;
    }

    public static Film mapToFilm(NewFilmRequestDto dto) {
        Film film = new Film();
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        film.setLikedByUsers(new HashSet<>());
        film.setGenres(new ArrayList<>());

        if (dto.getLikedByUsers() != null && !dto.getLikedByUsers().isEmpty()) {
            film.getLikedByUsers().addAll(dto.getLikedByUsers());
        }
        if (dto.getMpa() != null) {
            film.setRating(new FilmRating(dto.getMpa().getId(), null));
        }
        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            film.setGenres(dto.getGenres().stream().map(FilmGenreDto::getId).collect(Collectors.toList()));
        }
        return film;
    }


    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setLikedByUsers(film.getLikedByUsers());
        dto.setGenres(new ArrayList<>());
        dto.setMpa(new FilmRatingDto(film.getRating().getId(), film.getRating().getName()));

        return dto;
    }

}
