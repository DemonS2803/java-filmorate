package ru.yandex.practicum.filmorate.mapper;

import java.util.HashSet;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

public class UserMapper {

    public static User mapToUser(UserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());
        if (dto.getName() != null) {
            user.setName(dto.getName());
        } else {
            user.setName(dto.getLogin());
        }
        user.setBirthday(dto.getBirthday());
        user.setFriends(new HashSet<>());
        if (dto.getFriends() != null && !dto.getFriends().isEmpty()) {
            user.getFriends().addAll(dto.getFriends());
        }
        return user;
    }

}
