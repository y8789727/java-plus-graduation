package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class RequestStatsDto {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime start;
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime end;
    private List<String> uris;
    private Boolean unique;
}
