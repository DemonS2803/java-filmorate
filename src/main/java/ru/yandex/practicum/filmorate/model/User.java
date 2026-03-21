package ru.yandex.practicum.filmorate.model;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * User.
 */
@Getter
@Setter
@ToString
public class User {

    Long id;
    String email;
    String login;
    String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date birthday;
    Set<Long> friends;

}
