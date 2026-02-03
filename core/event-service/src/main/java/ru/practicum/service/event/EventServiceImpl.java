package ru.practicum.service.event;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.AdminEventParam;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPrivateParam;
import ru.practicum.dto.event.EventPublicParam;

import ru.practicum.dto.event.EventRequestStatusUpdateRequestParam;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.UpdateEventUserRequestParam;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.request.RequestStatusUpdateParam;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserDtoMapper;
import ru.practicum.ewm.client.stats.CollectorClient;
import ru.practicum.ewm.client.stats.RecommendationsClient;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.category.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventSort;
import ru.practicum.model.event.mapper.EventMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.util.OffsetBasedPageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final long MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN = 1;
    private static final long MIN_HOURS_BEFORE_UPDATE_FOR_USER = 2;

    private static final long MAX_RECOMMENDATIONS = 10;

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final CategoryRepository categoryRepository;
    private final RecommendationsClient recommendationsClient;
    private final CollectorClient collectorClient;

    private final EventMapper eventMapper;
    private final RequestClient requestClient;

    @Override
    public List<EventFullDto> findAllAdmin(AdminEventParam params) {
        int from = params.from();
        int size = params.size();
        Pageable pageable = new OffsetBasedPageable(from, size);

        List<Event> events = eventRepository
                .findAll(EventRepository.Predicate.adminFilters(params), pageable).getContent();

        setRatingAndConfirmedRequests(events);

        final Map<Long, UserDto> users = getUsersByEvents(events);

        return events.stream()
                .map(event -> eventMapper.toFullDto(event, UserDtoMapper.mapUserDtoToUserShortDto(users.get(event.getInitiatorId()))))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(long id, UpdateEventAdminRequest updateRequest) {
        Event event = getEventById(id);

        updateEvent(event, updateRequest);

        setRatingAndConfirmedRequests(event);
        return eventMapper.toFullDto(event, UserDtoMapper.mapUserDtoToUserShortDto(getUserById(event.getInitiatorId())));
    }

    @Override
    public List<EventShortDto> findPublicEvents(EventPublicParam params) {
        int from = params.from();
        int size = params.size();
        Sort defaultSort = Sort.by("eventDate");
        Pageable pageable = new OffsetBasedPageable(from, size, defaultSort);
        BooleanBuilder predicate;

        if (params.onlyAvailable() != null && params.onlyAvailable()) {
            List<Long> availableIds = findAvailableEvents();
            if (availableIds.isEmpty()) {
                return Collections.emptyList();
            }
            predicate = EventRepository.Predicate.publicFilters(params, availableIds);
        } else {
            predicate = EventRepository.Predicate.publicFilters(params);
        }

        List<Event> events = eventRepository.findAll(predicate, pageable).getContent();

        setRatingAndConfirmedRequests(events);

        Comparator<EventShortDto> comparator = createEventShortDtoComparator(params.sort());

        final Map<Long, UserDto> users = getUsersByEvents(events);

        return events.stream()
                .map(event -> eventMapper.toShortDto(event, UserDtoMapper.mapUserDtoToUserShortDto(users.get(event.getInitiatorId()))))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findPublicEventById(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED).orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found", eventId)));
        setRatingAndConfirmedRequests(event);
        return eventMapper.toFullDto(event, UserDtoMapper.mapUserDtoToUserShortDto(getUserById(event.getInitiatorId())));
    }

    @Override
    public List<EventShortDto> findUserEvents(Long userId, EventPrivateParam params) {
        int from = params.from();
        int size = params.size();
        Sort defaultSort = Sort.by("id").descending();

        getUserById(userId);

        Pageable pageable = new OffsetBasedPageable(from, size, defaultSort);
        return getShortDtoListByEvents(eventRepository.findAllByInitiatorId(userId, pageable));
    }

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        UserDto user = getUserById(userId);

        Category category = categoryRepository.findById(dto.category()).orElseThrow(
                () -> new NotFoundException(String.format("Category with id %d not found", dto.category())));

        Event event = eventRepository.save(eventMapper.toEntity(dto, user.id(), category));

        return eventMapper.toFullDto(event, UserDtoMapper.mapUserDtoToUserShortDto(getUserById(event.getInitiatorId())));
    }

    @Override
    public EventFullDto findUserEventById(Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id %d by user %d not found", eventId, userId))
        );
        setRatingAndConfirmedRequests(event);
        return eventMapper.toFullDto(event, UserDtoMapper.mapUserDtoToUserShortDto(getUserById(event.getInitiatorId())));
    }

    @Transactional
    @Override
    public EventFullDto updateUserEvent(UpdateEventUserRequestParam requestParam) {
        Event event = eventRepository.findByIdAndInitiatorId(requestParam.eventId(), requestParam.userId())
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        "Event with id %d by user %d not found",
                                        requestParam.eventId(),
                                        requestParam.userId())));

        UpdateEventUserRequest updateRequest = requestParam.request();

        updateEvent(event, updateRequest);
        setRatingAndConfirmedRequests(event);

        return eventMapper.toFullDto(event, UserDtoMapper.mapUserDtoToUserShortDto(getUserById(event.getInitiatorId())));
    }

    @Override
    public List<ParticipationRequestDto> findEventRequests(Long eventId, Long userId) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ValidationException("Пользователь " + userId + " не может запрашивать информацию по событию " + eventId);
        }

        return requestClient.getRequestsByEvent(event.getId());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequestParam requestParam) {
        Event event = eventRepository.findByIdAndInitiatorId(requestParam.eventId(), requestParam.userId())
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        "Event with id %d by user %d not found",
                                        requestParam.eventId(),
                                        requestParam.userId())));

        setConfirmedRequests(event);

        if (event.getParticipantLimit() != 0 &&
            event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConditionsNotMetException("Достигнут лимит по заявкам на событие - " + event.getId());
        }

        return requestClient.updateRequestsStatus(RequestStatusUpdateParam.builder()
                .event(eventMapper.toFullDto(event, UserDtoMapper.mapUserDtoToUserShortDto(getUserById(event.getInitiatorId()))))
                .updateRequest(requestParam.updateRequest())
                .build());
    }

    @Override
    public EventFullDto findEventById(Long eventId) {
        Event event = getEventById(eventId);
        return eventMapper.toFullDto(event, UserDtoMapper.mapUserDtoToUserShortDto(getUserById(event.getInitiatorId())));
    }

    @Override
    public List<EventShortDto> getEventsRecommendations(Long userId) {
        List<Long> recommendedEvents = recommendationsClient.getRecommendationsForUser(userId, MAX_RECOMMENDATIONS).stream()
                .map(RecommendedEventProto::getEventId)
                .toList();

        return getShortDtoListByEvents(eventRepository.findAllById(recommendedEvents));
    }

    @Override
    public void likeEvent(Long eventId, Long userId) {
        Optional<ParticipationRequestDto> request = requestClient.getRequestByEventAndUser(eventId, userId);

        if (request.isEmpty() || (!RequestStatus.CONFIRMED.equals(request.get().getStatus()))) {
            throw new ValidationException("Пользователь id=" + userId + " не подтвержден на мероприятие " + eventId);
        }

        sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    @Override
    public void sendUserAction(Long userId, Long eventId, ActionTypeProto actionType) {
        try {
            collectorClient.sendUserAction(userId, eventId, actionType);
        } catch (Exception e) {
            log.warn("Failed to save statistics for userId={}, eventId={}", userId, eventId, e);
        }
    }

    private Map<Long, Double> getRatingForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return recommendationsClient.getInteractionsCount(eventIds).stream()
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }

    private Long getConfirmedRequests(Long eventId) {
        if (eventId == null) {
            return 0L;
        }
        return requestClient.countRequestsByEventAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
        return requestClient.getConfirmedRequestsByEvents(eventIds);
    }

    private void setRatingAndConfirmedRequests(Event event) {
        setRating(event);
        setConfirmedRequests(event);
    }

    private void setRatingAndConfirmedRequests(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Double> ratingMap = getRatingForEvents(eventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsForEvents(eventIds);

        events.forEach(event -> {
            event.setRating(ratingMap.getOrDefault(event.getId(), 0D));
            event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
        });
    }

    private void setRating(Event event) {
        event.setRating(getRatingForEvents(List.of(event.getId())).getOrDefault(event.getId(), 0D));
    }

    private void setConfirmedRequests(Event event) {
        event.setConfirmedRequests(getConfirmedRequests(event.getId()));
    }

    private void updateEvent(Event event, UpdateEventUserRequest updateRequest) {
        LocalDateTime now = LocalDateTime.now();

        if (!event.getState().equals(State.PENDING) && !event.getState().equals(State.CANCELED)) {
            throw new ConditionsNotMetException("Ожидается статус PENDING или CANCELED, получен - " + event.getState());
        }

        if (now.plusHours(MIN_HOURS_BEFORE_UPDATE_FOR_USER).isAfter(event.getEventDate())) {
            throw new ConditionsNotMetException("Изменить можно события запланированные " +
                                                "на время не ранее чем через 2 часа от текущего, разница времени - " +
                                                Duration.between(now, event.getEventDate()).toHours());
        }

        Optional.ofNullable(updateRequest.annotation())
                .filter(ann -> !ann.isBlank()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateRequest.description())
                .filter(desc -> !desc.isBlank()).ifPresent(event::setDescription);
        Optional.ofNullable(updateRequest.eventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(updateRequest.location()).ifPresent(event::setLocation);
        Optional.ofNullable(updateRequest.paid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateRequest.participantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateRequest.requestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateRequest.title()).filter(title -> !title.isBlank()).ifPresent(event::setTitle);

        Optional.ofNullable(updateRequest.category()).ifPresent(
                categoryId -> event.setCategory(categoryRepository.findById(categoryId)
                        .orElseThrow(
                                () -> new NotFoundException("Category id " + categoryId + " not found")
                        ))
        );

        if (updateRequest.stateAction() != null) {
            switch (updateRequest.stateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }
    }

    private void updateEvent(Event event, UpdateEventAdminRequest updateRequest) {
        LocalDateTime now = LocalDateTime.now();

        if (event.getState() != State.PENDING) {
            throw new ConditionsNotMetException(
                    "Событие можно публиковать или отклонить, только если оно в состоянии ожидания публикации. Настоящее состояние: "
                    + event.getState());
        }

        if (now.plusHours(MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN).isAfter(event.getEventDate())) {
            throw new ConditionsNotMetException(
                    "Дата начала изменяемого события должна быть не ранее чем за час от даты публикации, разница времени - " +
                    Duration.between(now, event.getEventDate()).toHours());
        }

        Optional.ofNullable(updateRequest.annotation())
                .filter(ann -> !ann.isBlank()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateRequest.description())
                .filter(desc -> !desc.isBlank()).ifPresent(event::setDescription);
        Optional.ofNullable(updateRequest.eventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(updateRequest.location()).ifPresent(event::setLocation);
        Optional.ofNullable(updateRequest.paid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateRequest.participantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateRequest.requestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateRequest.title()).filter(title -> !title.isBlank()).ifPresent(event::setTitle);

        Optional.ofNullable(updateRequest.category()).ifPresent(
                categoryId -> event.setCategory(categoryRepository.findById(categoryId)
                        .orElseThrow(
                                () -> new NotFoundException("Category id " + categoryId + " not found")
                        ))
        );

        if (updateRequest.stateAction() != null) {
            switch (updateRequest.stateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> event.setState(State.CANCELED);
            }
        }
    }

    private Comparator<EventShortDto> createEventShortDtoComparator(EventSort sort) {
        Comparator<EventShortDto> comparator;

        if (sort == null) {
            comparator = (a, b) -> 0;
        } else {
            comparator = switch (sort) {
                case VIEWS -> Comparator.comparing(EventShortDto::rating).reversed();
                case EVENT_DATE -> Comparator.comparing(EventShortDto::eventDate);
            };
        }
        return comparator;
    }

    private UserDto getUserById(Long userId) {
        return userClient.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Map<Long, UserDto> getUsersByEvents(List<Event> events) {
        List<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .toList();

        return userClient.findByIds(userIds, 0, userIds.size()).stream()
                .collect(Collectors.toMap(UserDto::id, Function.identity()));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдена"));
    }

    private List<Long> findAvailableEvents() {

        List<Long> availableEventIds = new ArrayList<>(eventRepository.findPublishedEventIdsNoLimit());

        List<Event> eventsWithLimits = eventRepository.findPublishedEventsWithLimit();

        final Map<Long, Long> countRequests = requestClient.getConfirmedRequestsByEvents(eventsWithLimits.stream()
                                                                                    .map(Event::getId)
                                                                                    .toList());
        availableEventIds.addAll(eventsWithLimits.stream()
                .filter(event -> {
                    if (countRequests.containsKey(event.getId())) {
                        return event.getParticipantLimit() > countRequests.get(event.getId());
                    } else {
                        return true;
                    }
                })
                .map(Event::getId)
                .toList());

        return availableEventIds;
    }

    private List<EventShortDto> getShortDtoListByEvents(List<Event> events) {
        setRatingAndConfirmedRequests(events);

        final Map<Long, UserDto> users = getUsersByEvents(events);

        return events.stream()
                .map(event -> eventMapper.toShortDto(event, UserDtoMapper.mapUserDtoToUserShortDto(users.get(event.getInitiatorId()))))
                .toList();
    }
}
