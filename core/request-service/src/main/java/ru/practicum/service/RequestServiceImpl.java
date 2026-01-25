package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.RequestDtoMapper;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.request.RequestStatusUpdateParam;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.AllreadyExistsException;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Request;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserClient userClient;
    private final RequestRepository requestRepository;
    private final EventClient eventClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        UserDto user = getUserById(userId);

        return requestRepository.findUserRequests(user.id()).stream()
                .map(RequestDtoMapper::mapRequestToDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        final UserDto user = getUserById(userId);
        final EventFullDto event = getEventById(eventId);

        validateCreation(user, event);

        return RequestDtoMapper.mapRequestToDto(requestRepository.save(Request.builder()
                .createdOn(LocalDateTime.now())
                .eventId(event.id())
                .requesterId(user.id())
                .status(event.requestModeration() && event.participantLimit() > 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build()));
    }

    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        UserDto user = getUserById(userId);
        Request request = getRequestById(requestId);

        if (!request.getRequesterId().equals(user.id())) {
            throw new ValidationException("Пользователь id=" + user.id() + " не может отменить заявку id=" + request.getId());
        }

        if (RequestStatus.CANCELED.equals(request.getStatus()) || RequestStatus.REJECTED.equals(request.getStatus())) {
            throw new ValidationException("Статус заявки " + request.getStatus() + " не позволяет выполнить отмену");
        }

        request.setStatus(RequestStatus.CANCELED);

        return RequestDtoMapper.mapRequestToDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long eventId) {
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestDtoMapper::mapRequestToDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(RequestStatusUpdateParam updateParams) {
        final EventFullDto event = updateParams.getEvent();
        final List<Request> requestsToUpdate = requestRepository.findAllById(updateParams.getUpdateRequest().requestIds());

        requestsToUpdate.forEach(request -> {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException(
                        "Статус можно изменить только у заявок в состоянии ожидания. " +
                        "Текущий статус заявки " + request.getId() + ": " + request.getStatus());
            }
        });

        if (event.participantLimit() == 0 || !event.requestModeration()) {
            requestsToUpdate.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
            return new EventRequestStatusUpdateResult(RequestDtoMapper.mapRequestToDto(requestsToUpdate), List.of());
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        long availableSlots = event.participantLimit() - event.confirmedRequests();

        for (Request request : requestsToUpdate) {
            if (availableSlots > 0 && updateParams.getUpdateRequest().status().equals(RequestStatus.CONFIRMED)) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
                availableSlots--;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }
        requestRepository.saveAll(requestsToUpdate);

        return new EventRequestStatusUpdateResult(
                RequestDtoMapper.mapRequestToDto(confirmedRequests),
                RequestDtoMapper.mapRequestToDto(rejectedRequests)
        );
    }

    @Override
    public Long countByEventAndStatus(Long eventId, RequestStatus status) {
        return requestRepository.countByEventAndStatus(eventId, status);
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsByEvents(List<Long> eventIds) {
        try {
            return requestRepository.countConfirmedRequestsByEventIds(eventIds).stream()
                    .collect(Collectors.toMap(
                            e -> (Long) e[0],
                            e -> (Long) e[1]
                    ));
        } catch (Exception e) {
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }

    private UserDto getUserById(long userId) {
        return userClient.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Request getRequestById(long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request " + requestId + " not found!"));
    }

    private EventFullDto getEventById(long eventId) {
        return eventClient.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event " + eventId + " not found!"));
    }

    private void validateCreation(UserDto user, EventFullDto event) {
        if (user.id().equals(event.initiator().id())) {
            throw new AllreadyExistsException("Пользователя " + user.id() + " не может добавить запрос на участие в своем событии " + event.id());
        }

        if (!State.PUBLISHED.equals(event.state())) {
            throw new AllreadyExistsException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.findByUserIdAndEvent(user.id(), event.id()).isPresent()) {
            throw new AllreadyExistsException("Для пользователя " + user.id() + " уже существует запрос на участие в событие " + event.id());
        }

        if (event.participantLimit() != 0 && requestRepository.countByEventAndStatus(event.id(), RequestStatus.CONFIRMED) >= event.participantLimit()) {
            throw new AllreadyExistsException("Достигнут лимит запросов на участие " + event.id());
        }
    }
}
