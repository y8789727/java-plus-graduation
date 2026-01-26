package ru.practicum.client;

import org.springframework.stereotype.Component;
import ru.practicum.dto.event.EventFullDto;

import java.util.Optional;

@Component
public class EventClientFallback implements EventClient {
    @Override
    public Optional<EventFullDto> getEventById(Long eventId) {
        return Optional.empty();
    }
}
