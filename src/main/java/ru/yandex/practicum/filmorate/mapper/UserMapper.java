package ru.yandex.practicum.filmorate.mapper;

import java.util.HashSet;

import ru.yandex.practicum.filmorate.dto.NewUserRequestDto;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequestDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

public class UserMapper {

    public static User mapToUser(UpdateUserRequestDto dto) {
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



    public static User mapToUser(NewUserRequestDto dto) {
        User user = new User();
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

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setBirthday(user.getBirthday());
        dto.setFriends(user.getFriends());
        return dto;
    }

}
