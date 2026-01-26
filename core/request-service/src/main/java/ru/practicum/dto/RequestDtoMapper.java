package ru.practicum.dto;

import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.Request;

import java.util.List;

public class RequestDtoMapper {
    public static ParticipationRequestDto mapRequestToDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .status(request.getStatus())
                .created(request.getCreatedOn())
                .eventId(request.getEventId())
                .requesterId(request.getRequesterId())
                .build();
    }

    public static List<ParticipationRequestDto> mapRequestToDto(List<Request> requests) {
        return requests.stream().map(RequestDtoMapper::mapRequestToDto).toList();
    }
}
