package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

@Builder
@Getter
public class ParticipationRequestDto {
    private final Long id;
    private final RequestStatus status;
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private final LocalDateTime created;
    @JsonProperty("event")
    private final Long eventId;
    @JsonProperty("requester")
    private final Long requesterId;
}