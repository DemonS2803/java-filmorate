package ru.yandex.practicum.filmorate.dto;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateFilmRequestDto {

    @NotNull(message = "ID фильма не может быть пустым")
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @Size(max = 200, message = "Описание не более 200 символов")
    private String description;
    @NotNull(message = "Дата релиза не может быть null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    @NotNull(message = "Продолжительность должна быть положительной")
    @Positive(message = "Продолжительность должна быть положительной")
    private Integer duration;
    private Set<Long> likedByUsers;
    @NotNull(message = "Рейтинг фильма не может быть пустым")
    private FilmRatingDto mpa;
    private List<FilmGenreDto> genres;

    @AssertTrue(message = "Фильм выпущен не ранее 18.12.1895")
    private boolean isNotBeforeFirstFilm() {
        if (releaseDate == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(1895, Calendar.DECEMBER, 27, 23, 59, 59);
        LocalDate dateBeforeFirstFilm = LocalDate.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).atStartOfDay().toLocalDate();
        return releaseDate.isAfter(dateBeforeFirstFilm);
    }

}
