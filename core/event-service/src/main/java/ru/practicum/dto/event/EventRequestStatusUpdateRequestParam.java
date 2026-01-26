package ru.practicum.dto.event;

import ru.practicum.dto.request.EventRequestStatusUpdateRequest;

public record EventRequestStatusUpdateRequestParam(
        Long userId,
        Long eventId,
        EventRequestStatusUpdateRequest updateRequest
) {
}
