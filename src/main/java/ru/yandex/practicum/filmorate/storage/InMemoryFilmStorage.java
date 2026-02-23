package ru.yandex.practicum.filmorate.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.exceptions.NoFilmFoundException;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long filmCurrentId = 1L;


    @Override
    public List<Film> findAll() {
        return films.values().stream().toList();
    }

    @Override
    public Optional<Film> findFilmById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film save(Film film) {
        if (film.getId() == null) {
            log.info("Film id is empty. Set new id: {}", filmCurrentId);
            film.setId(filmCurrentId++);
        }
        if (films.containsKey(film.getId())) {
            log.warn("Film with id {} already exists. Set new id: {}", film.getId(), filmCurrentId);
            film.setId(filmCurrentId++);
        }
        log.info("User added new film: {}", film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new InvalidFilmDataException("Film id is empty. Failed to update film");
        }
        if (!films.containsKey(film.getId())) {
            throw new NoFilmFoundException("Film with id " + film.getId() + " does not exist");
        }

        log.info("User updated film with id {}", film.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public boolean delete(long id) {
        return films.remove(id) != null;
    }

}
