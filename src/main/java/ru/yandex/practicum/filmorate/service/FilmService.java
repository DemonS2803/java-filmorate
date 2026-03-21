package ru.yandex.practicum.filmorate.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequestDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequestDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmGenreFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmRatingFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final FilmRatingService filmRatingService;
    private final FilmGenreService filmGenreService;

    public List<FilmDto> getFilms() {
        log.debug("Get all films");
        List<FilmDto> dtos = filmStorage.findAll().stream().map(FilmMapper::mapToFilmDto).toList();
        loadFilmGenresForFilmDtos(dtos);
        return dtos;
    }

    public FilmDto getFilmById(final long id) {
        log.debug("Get film by id: {}", id);
        Film film = getFilmByIdOrThrow(id);
        FilmDto dto = FilmMapper.mapToFilmDto(film);
        dto.setGenres(filmGenreService.getFilmGenresByFilmId(id));
        return dto;
    }

    // И если уж говорить про оптимизацию запросов к БД, то первым кандидатом на вылет является рейтинг фильмов.
    // Кто придумал хранить его в бд и на каждый запрос к ключевой сущности сервиса делать join?
    // Я знаю, что join быстрее отдельного запроса, но все же он съедает ресурсы
    private Film getFilmByIdOrThrow(long id) {
        Film film = filmStorage.findFilmById(id)
                .orElseThrow(() -> new NoFilmFoundException("No film with id " + id + " found"));
        return film;
    }

    public FilmDto createFilm(NewFilmRequestDto dto) {
        Film film = FilmMapper.mapToFilm(dto);
        log.info("Create film: {}", film);
        // validations
        checkFilmGenresExists(film);
        checkFilmRatingValid(film);

        film = filmStorage.save(film);
        filmGenreService.saveFilmGenresForFilm(film.getId(), film.getGenres());
        FilmDto responseDto = FilmMapper.mapToFilmDto(film);
        responseDto.setGenres(filmGenreService.getFilmGenresByFilmId(film.getId()));
        responseDto.setMpa(filmRatingService.getFilmRatingById(dto.getMpa().getId()));
        return responseDto;
    }

    public FilmDto updateFilm(UpdateFilmRequestDto dto) {
        Film film = FilmMapper.mapToFilm(dto);
        // check for film exists
        getFilmById(film.getId());
        // validations
        checkFilmGenresExists(film);
        checkFilmRatingValid(film);

        log.info("Update film: {}", film);
        film = filmStorage.update(film);
        filmGenreService.saveFilmGenresForFilm(film.getId(), film.getGenres());

        FilmDto responseDto = FilmMapper.mapToFilmDto(film);
        responseDto.setGenres(filmGenreService.getFilmGenresByFilmId(film.getId()));
        responseDto.setMpa(filmRatingService.getFilmRatingById(dto.getMpa().getId()));
        return responseDto;
    }

    public FilmDto likeFilm(Long userId, Long filmId) {
        log.info("Like film: {}", filmId);
        Film film = getFilmByIdOrThrow(filmId);
        // check for user exists
        userService.getUserById(userId);

        filmStorage.likeFilm(filmId, userId);
        film.getLikedByUsers().add(userId);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto unlikeFilm(Long userId, Long filmId) {
        log.info("Unlike film: {}", filmId);
        Film film = getFilmByIdOrThrow(filmId);
        // check for user exists
        userService.getUserById(userId);

        filmStorage.unlikeFilm(filmId, userId);
        film.getLikedByUsers().remove(userId);

        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getPopularFilms(Integer count) {
        log.debug("Get popular films");
        if (count == null || count <= 0) {
            log.error("Get popular films: count must be greater than 0");
            return new ArrayList<>();
        }

        List<FilmDto> dtos = filmStorage.findMostPopularFilms(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
        loadFilmGenresForFilmDtos(dtos);
        return dtos;
    }

    private void checkFilmGenresExists(Film film) {
        // will throw exception if any not exists
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        if (!filmGenreService.isAllFilmGenresExists(film.getGenres())) {
            throw new NoFilmGenreFoundException("No film genres found");
        }

    }

    private void checkFilmRatingValid(Film film) {
        if (film.getRating() == null) {
            throw new NoFilmRatingFoundException("No film rating found");
        }
        filmRatingService.getFilmRatingById(film.getRating().getId());
    }

    // По тз, нам надо возвращать только id жанров
    // "При создании и получении фильмов достаточно передать список идентификаторов жанров и идентификатор рейтинга"
    // Я понимаю, что это скорее всего косяк при копировании ТЗ, но все же....
    private void loadFilmGenresForFilmDtos(List<FilmDto> filmDtos) {
        List<Long> filmIds = filmDtos.stream().map(FilmDto::getId).collect(Collectors.toList());
        Map<Long, Set<FilmGenre>> filmGenresMap = filmGenreService.getFilmGenresByFilmsIds(filmIds);

        filmDtos.forEach(dto -> {
            Set<FilmGenre> filmGenresSet = filmGenresMap.getOrDefault(dto.getId(), new HashSet<>());
            List<FilmGenre> filmGenres = filmGenresSet.stream().toList();
            dto.setGenres(filmGenres.stream().map(FilmGenreMapper::toDto).toList());
        });
    }

}
