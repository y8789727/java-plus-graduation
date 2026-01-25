package ru.practicum.dto.compilation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import ru.practicum.dto.event.EventShortDto;

import java.util.Set;

@Builder
@Getter
public class CompilationDto {
    private final Long id;
    private final String title;
    private final boolean pinned;
    @JsonProperty("events")
    private final Set<EventShortDto> eventIds;
}
