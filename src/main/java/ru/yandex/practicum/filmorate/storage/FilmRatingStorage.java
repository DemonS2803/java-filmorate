package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.List;
import java.util.Optional;

public interface FilmRatingStorage {

    Optional<FilmRating> findById(int id);

    List<FilmRating> findAll();

}
