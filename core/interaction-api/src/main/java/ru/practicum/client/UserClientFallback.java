package ru.practicum.client;

import ru.practicum.dto.user.UserDto;

import java.util.List;
import java.util.Optional;

public class UserClientFallback implements UserClient {
    @Override
    public Optional<UserDto> findById(Long userId) {
        return Optional.empty();
    }

    @Override
    public List<UserDto> findByIds(List<Long> ids, Integer from, Integer size) {
        return List.of();
    }
}
