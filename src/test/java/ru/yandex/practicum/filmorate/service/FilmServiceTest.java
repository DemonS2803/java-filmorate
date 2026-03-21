package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.NoFilmFoundException;
import ru.yandex.practicum.filmorate.exceptions.NoUserFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserService userService;

    @Mock
    private FilmGenreService filmGenreService;

    @InjectMocks
    private FilmService filmService;

    private Film film1;
    private Film film2;
    private Film film3;
    private UserDto user1;
    private UserDto user2;
    private UserDto user3;
    private LocalDate releaseDate;

    @BeforeEach
    void setUp() {
        releaseDate = LocalDate.of(2020, 1, 1);

        film1 = new Film();
        film1.setId(1L);
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(releaseDate);
        film1.setDuration(120);
        film1.setLikedByUsers(new HashSet<>());
        film1.setRating(FilmRating.PG_13);

        film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(releaseDate);
        film2.setDuration(90);
        film2.setLikedByUsers(new HashSet<>());
        film2.setRating(FilmRating.PG_13);

        film3 = new Film();
        film3.setId(3L);
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(releaseDate);
        film3.setDuration(150);
        film3.setLikedByUsers(new HashSet<>());
        film3.setRating(FilmRating.PG_13);

        user1 = new UserDto();
        user1.setId(1L);
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("UserDto One");

        user2 = new UserDto();
        user2.setId(2L);
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("UserDto Two");

        user3 = new UserDto();
        user3.setId(3L);
        user3.setEmail("user3@test.com");
        user3.setLogin("user3");
        user3.setName("UserDto Three");
    }


    @Test
    void likeFilm_ShouldThrowException_WhenFilmNotFound() {
        when(filmStorage.findFilmById(99L)).thenReturn(Optional.empty());

        assertThrows(NoFilmFoundException.class, () ->
                filmService.likeFilm(1L, 99L)
        );

        verify(filmStorage).findFilmById(99L);
        verify(userService, never()).getUserById(1L);
        verify(filmStorage, never()).update(any());
    }

    @Test
    void likeFilm_ShouldThrowException_WhenUserNotFound() {
        when(filmStorage.findFilmById(1L)).thenReturn(Optional.ofNullable(film1));
        when(userService.getUserById(99L)).thenThrow(new NoUserFoundException(""));

        assertThrows(NoUserFoundException.class, () ->
                filmService.likeFilm(99L, 1L)
        );

        verify(filmStorage).findFilmById(1L);
        verify(userService).getUserById(99L);
        verify(filmStorage, never()).update(any());
    }

    @Test
    void likeFilm_ShouldReturnUpdatedFilm_WhenLikeAdded() {
        when(filmStorage.findFilmById(1L)).thenReturn(Optional.ofNullable(film1));
        when(userService.getUserById(1L)).thenReturn(user1);

        FilmDto result = filmService.likeFilm(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Film 1", result.getName());
    }


    @Test
    void unlikeFilm_ShouldDoNothing_WhenUserDidNotLikeFilm() {
        when(filmStorage.findFilmById(1L)).thenReturn(Optional.ofNullable(film1));
        when(userService.getUserById(1L)).thenReturn(user1);

        FilmDto result = filmService.unlikeFilm(1L, 1L);

        assertFalse(result.getLikedByUsers().contains(1L));
        assertEquals(0, result.getLikedByUsers().size());

        verify(filmStorage, times(2)).findFilmById(1L);
        verify(userService).getUserById(1L);
    }

    @Test
    void unlikeFilm_ShouldThrowException_WhenFilmNotFound() {
        when(filmStorage.findFilmById(99L)).thenThrow(new NoFilmFoundException(""));

        assertThrows(NoFilmFoundException.class, () ->
                filmService.unlikeFilm(1L, 99L)
        );

        verify(filmStorage).findFilmById(99L);
        verify(userService, never()).getUserById(1L);
        verify(filmStorage, never()).update(any());
    }

    @Test
    void unlikeFilm_ShouldThrowException_WhenUserNotFound() {
        when(filmStorage.findFilmById(1L)).thenReturn(Optional.ofNullable(film1));
        when(userService.getUserById(99L)).thenThrow(new NoUserFoundException(""));
        when(filmGenreService.getFilmGenresIdsByFilmId(1L)).thenReturn(new HashSet<>());

        assertThrows(NoUserFoundException.class, () ->
                filmService.unlikeFilm(99L, 1L)
        );

        verify(filmStorage).findFilmById(1L);
        verify(userService).getUserById(99L);
        verify(filmStorage, never()).update(any());
    }

    @Test
    void unlikeFilm_ShouldReturnUpdatedFilm_WhenLikeRemoved() {
        film1.getLikedByUsers().add(1L);

        when(filmStorage.findFilmById(1L)).thenReturn(Optional.ofNullable(film1));
        when(userService.getUserById(1L)).thenReturn(user1);

        FilmDto result = filmService.unlikeFilm(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Film 1", result.getName());
    }

    @Test
    void getPopularFilms_ShouldReturnFilmsOrderedByLikesCount() {
        film1.getLikedByUsers().addAll(Set.of(1L, 2L, 3L));
        film2.getLikedByUsers().addAll(Set.of(1L, 2L));
        film3.getLikedByUsers().add(1L);

        List<Film> popularFilms = new ArrayList<>();
        popularFilms.add(film1);
        popularFilms.add(film2);
        popularFilms.add(film3);

        when(filmStorage.findMostPopularFilms(3)).thenReturn(popularFilms);

        List<FilmDto> result = filmService.getPopularFilms(3);

        assertEquals(3, result.size());
        Iterator<FilmDto> iterator = result.iterator();
        assertEquals(1L, iterator.next().getId());
        assertEquals(2L, iterator.next().getId());
        assertEquals(3L, iterator.next().getId());

        verify(filmStorage).findMostPopularFilms(3);
    }

    @Test
    void getPopularFilms_ShouldReturnLimitedNumberOfFilms_WhenCountSpecified() {
        film1.getLikedByUsers().addAll(Set.of(1L, 2L, 3L));
        film2.getLikedByUsers().addAll(Set.of(1L, 2L));

        List<Film> popularFilms = new ArrayList<>();
        popularFilms.add(film1);
        popularFilms.add(film2);

        when(filmStorage.findMostPopularFilms(2)).thenReturn(popularFilms);

        List<FilmDto> result = filmService.getPopularFilms(2);

        assertEquals(2, result.size());
        verify(filmStorage).findMostPopularFilms(2);
    }

    @Test
    void getPopularFilms_ShouldReturnEmptySet_WhenNoFilmsExist() {
        when(filmStorage.findMostPopularFilms(10)).thenReturn(new ArrayList<>());

        List<FilmDto> result = filmService.getPopularFilms(10);

        assertTrue(result.isEmpty());
        verify(filmStorage).findMostPopularFilms(10);
    }

    @Test
    void getPopularFilms_ShouldReturnAllFilms_WhenCountGreaterThanTotal() {
        film1.getLikedByUsers().addAll(Set.of(1L, 2L, 3L));
        film2.getLikedByUsers().addAll(Set.of(1L, 2L));
        film3.getLikedByUsers().add(1L);

        List<Film> allFilms = new ArrayList<>();
        allFilms.add(film1);
        allFilms.add(film2);
        allFilms.add(film3);

        when(filmStorage.findMostPopularFilms(10)).thenReturn(allFilms);

        List<FilmDto> result = filmService.getPopularFilms(10);

        assertEquals(3, result.size());
        verify(filmStorage).findMostPopularFilms(10);
    }

    @Test
    void getPopularFilms_ShouldHandleFilmsWithSameLikesCount() {
        film1.getLikedByUsers().addAll(Set.of(1L, 2L));
        film2.getLikedByUsers().addAll(Set.of(3L, 4L));
        film3.getLikedByUsers().add(5L);

        List<Film> popularFilms = new ArrayList<>();
        popularFilms.add(film1);
        popularFilms.add(film2);
        popularFilms.add(film3);

        when(filmStorage.findMostPopularFilms(3)).thenReturn(popularFilms);

        List<FilmDto> result = filmService.getPopularFilms(3);

        assertEquals(3, result.size());
        verify(filmStorage).findMostPopularFilms(3);
    }
}