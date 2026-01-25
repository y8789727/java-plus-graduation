package ru.practicum.client;

import ru.practicum.dto.event.EventFullDto;

import java.util.Optional;

public class EventClientFallback implements EventClient {
    @Override
    public Optional<EventFullDto> getEventById(Long eventId) {
        return Optional.empty();
    }
}
