package ru.practicum.service;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserParam;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto save(NewUserRequest user);

    List<UserDto> findAll(UserParam userParam);

    void deleteById(Long userId);

    UserDto findById(Long userId);
}