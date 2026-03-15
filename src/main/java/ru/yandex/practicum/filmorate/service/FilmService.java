package ru.yandex.practicum.filmorate.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequestDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequestDto;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final FilmGenreService filmGenreService;

    public List<FilmDto> getFilms() {
        log.debug("Get all films");
        return filmStorage.findAll().stream().map(this::convertToDto).toList();
    }

    public FilmDto getFilmById(final long id) {
        log.debug("Get film by id: {}", id);
        return convertToDto(getFilmByIdOrThrow(id));
    }

    private Film getFilmByIdOrThrow(long id) {
        return filmStorage.findFilmById(id)
                .orElseThrow(() -> new NoFilmFoundException("No film with id " + id + " found"));
    }

    public FilmDto createFilm(NewFilmRequestDto dto) {
        Film film = FilmMapper.mapToFilm(dto);
        log.info("Create film: {}", film);
        // validations
        checkFilmGenresExists(film);
        checkFilmRatingValid(film);

        Film savedFilm = filmStorage.save(film);

        return convertToDto(savedFilm);
    }

    public FilmDto updateFilm(UpdateFilmRequestDto dto) {
        Film film = FilmMapper.mapToFilm(dto);
        // check for film exists
        getFilmById(film.getId());
        // validations
        checkFilmGenresExists(film);
        checkFilmRatingValid(film);

        log.info("Update film: {}", film);
        Film updatedFilm = filmStorage.update(film);
        return convertToDto(updatedFilm);
    }

    public FilmDto likeFilm(Long userId, Long filmId) {
        log.info("Like film: {}", filmId);
        Film film = getFilmByIdOrThrow(filmId);
        // check for user exists
        userService.getUserById(userId);

        film.getLikedByUsers().add(userId);
        film = filmStorage.update(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto unlikeFilm(Long userId, Long filmId) {
        log.info("Unlike film: {}", filmId);
        Film film = getFilmByIdOrThrow(filmId);
        // check for user exists
        userService.getUserById(userId);

        film.getLikedByUsers().remove(userId);
        film = filmStorage.update(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getPopularFilms(Integer count) {
        log.debug("Get popular films");
        if (count == null || count <= 0) {
            log.error("Get popular films: count must be greater than 0");
            return new ArrayList<>();
        }
        return filmStorage.findMostPopularFilms(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    private FilmDto convertToDto(Film film) {
        log.debug("Convert {} to DTO object", film);
        FilmDto dto = FilmMapper.mapToFilmDto(film);
        loadFilmGenresToDto(dto, film);
        return dto;
    }

    private void checkFilmGenresExists(Film film) {
        // will throw exception if any not exists
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            filmGenreService.getFilmGenresByIds(film.getGenres());
        }
    }

    private void checkFilmRatingValid(Film film) {
        if (film.getRating() == null) {
            throw new NoFilmRatingFoundException("No film rating found");
        }
        if (FilmRating.valueOf(film.getRating().getId()) == null) {
            throw new NoFilmRatingFoundException("No Film rating found for id: " + film.getRating().getId());
        }
    }

    private void loadFilmGenresToDto(FilmDto dto, Film film) {
        List<FilmGenreDto> filmGenres = filmGenreService.getFilmGenresByIds(film.getGenres());
        log.debug("Load film genres to dto: {}", filmGenres);
        dto.setGenres(filmGenres);
    }

}
