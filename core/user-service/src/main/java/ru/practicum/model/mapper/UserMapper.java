package ru.practicum.model.mapper;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.User;

public class UserMapper {
    public static User mapToUser(UserDto userDto) {
        return User.builder().name(userDto.name()).email(userDto.email()).build();
    }

    public static User mapToUser(NewUserRequest userDto) {
        return User.builder().name(userDto.name()).email(userDto.email()).build();
    }

    public static UserDto mapToUserDto(User user) {
        return new UserDto(user.getEmail(), user.getId(), user.getName());
    }

    public static UserShortDto mapToUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}