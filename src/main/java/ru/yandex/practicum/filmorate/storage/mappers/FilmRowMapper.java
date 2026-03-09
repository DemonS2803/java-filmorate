package ru.yandex.practicum.filmorate.storage.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate);
        }

        film.setDuration(rs.getInt("duration"));
        film.setLikedByUsers(new HashSet<>());
        film.setRating(FilmRating.valueOf(rs.getInt("rating")));
        film.setGenres(new HashSet<>());

        return film;
    }

}
