package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.user.UserDto;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "user-service", dismiss404 = true, fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/admin/users/{userId}")
    Optional<UserDto> findById(@PathVariable(name = "userId") Long userId);

    @GetMapping("/admin/users")
    List<UserDto> findByIds (
            @RequestParam List<Long> ids,
            @RequestParam Integer from,
            @RequestParam Integer size
    );
}
