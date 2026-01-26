package ru.practicum.client;

import org.springframework.stereotype.Component;
import ru.practicum.dto.user.UserDto;

import java.util.List;
import java.util.Optional;

@Component
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
