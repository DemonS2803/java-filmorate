package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * User.
 */
@Getter
@Setter
@EqualsAndHashCode
public class User {

    Long id;
    String email;
    String login;
    String name;
    Date birthday;

}
