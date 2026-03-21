package ru.yandex.practicum.filmorate.dto;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class UserDto {

    Long id;
    String email;
    String login;
    String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date birthday;
    Set<Long> friends;
}
