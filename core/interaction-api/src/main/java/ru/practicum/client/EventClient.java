package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.event.EventFullDto;

import java.util.Optional;

@FeignClient(name = "event-service", dismiss404 = true, fallback = EventClientFallback.class)
public interface EventClient {
    @GetMapping("/admin/events/{eventId}")
    Optional<EventFullDto> getEventById(@PathVariable Long eventId);
}
