package ru.practicum.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicParam;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class PublicEventController {
    private final EventService eventService;

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findPublicEvents(@Valid @ModelAttribute EventPublicParam params) {
        log.info("Public: Method launched (findPublicEvents({}))", params);
        return eventService.findPublicEvents(params);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findPublicEventById(
            @Positive(message = "eventId должен быть больше 0") @PathVariable Long id,
            @RequestHeader("X-EWM-USER-ID") Long userId
    ) {
        log.info("Public: Method launched (findPublicEventById({}))", id);
        EventFullDto event = eventService.findPublicEventById(id);
        saveHit(userId, id);
        return event;
    }

    @GetMapping("/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Public: Method launched (getEventsRecommendations({}))", userId);
        return eventService.getEventsRecommendations(userId);
    }

    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public void likeEvent(@PathVariable("eventId") Long eventId,
                                         @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Public: Method launched (likeEvent({}, {}))", eventId, userId);
        eventService.likeEvent(eventId, userId);
    }

    private void saveHit(Long userId, Long eventId) {
        eventService.sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }
}
