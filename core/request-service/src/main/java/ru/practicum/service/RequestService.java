package ru.practicum.service;

import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.request.RequestStatusUpdateParam;

import java.util.List;
import java.util.Map;

public interface RequestService {
    List<ParticipationRequestDto> getUserRequests(long userId);

    ParticipationRequestDto createRequest(long userId, long eventId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);

    List<ParticipationRequestDto> getRequestsByEvent(Long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(RequestStatusUpdateParam updateParams);

    Long countByEventAndStatus(Long eventId, RequestStatus status);

    Map<Long, Long> getConfirmedRequestsByEvents(List<Long> eventIds);
}
