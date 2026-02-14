package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.dto.UserDto;

import java.util.Date;

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

    public static User of(UserDto dto) {
        User user = new User();
        user.id = dto.getId();
        user.email = dto.getEmail();
        user.login = dto.getLogin();
        if (dto.getName() != null) {
            user.name = dto.getName();
        } else {
            user.name = dto.getLogin();
        }
        user.birthday = dto.getBirthday();
        return user;
    }

}
