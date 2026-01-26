package ru.practicum.client;

import org.springframework.stereotype.Component;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.request.RequestStatusUpdateParam;

import java.util.List;
import java.util.Map;

@Component
public class RequestClientFallback implements RequestClient {
    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long eventId) {
        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(RequestStatusUpdateParam updateParams) {
        throw new RuntimeException("Request Service is unavailable!");
    }

    @Override
    public Long countRequestsByEventAndStatus(Long eventId, RequestStatus status) {
        return 0L;
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsByEvents(List<Long> eventIds) {
        return Map.of();
    }
}
