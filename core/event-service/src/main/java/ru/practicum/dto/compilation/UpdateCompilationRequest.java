package ru.practicum.dto.compilation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class UpdateCompilationRequest {
    private final Boolean pinned;
    @Size(min = 1, max = 50, message = "Заголовок должен быть не менее 1 и не более 50 символов")
    private final String title;
    @JsonProperty("events")
    private final Set<Long> eventIds;
}
