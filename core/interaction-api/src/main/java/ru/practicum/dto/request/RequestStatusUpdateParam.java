package ru.practicum.dto.request;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.dto.event.EventFullDto;

@Builder
@Getter
public class RequestStatusUpdateParam {
    private final EventFullDto event;
    private final EventRequestStatusUpdateRequest updateRequest;
}
