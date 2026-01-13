package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.controller.StatsController;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
public class StatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatsService statsService;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime hourAgo = now.minusHours(1);
    private final LocalDateTime twoHoursAgo = now.minusHours(2);

    @Test
    void saveHit_shouldReturnCreatedStatus() throws Exception {
        HitCreateDto createDto = new HitCreateDto(
                "ewm-main-service",
                "/events/1",
                "192.168.1.1",
                hourAgo
        );

        doNothing().when(statsService).saveHit(any(HitCreateDto.class));

        mvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        verify(statsService, times(1)).saveHit(any(HitCreateDto.class));
    }

    @Test
    void saveHit_shouldReturnBadRequestWhenAppIsBlank() throws Exception {
        HitCreateDto invalidDto = new HitCreateDto(
                "",
                "/events/1",
                "192.168.1.1",
                hourAgo
        );

        mvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(statsService, never()).saveHit(any());
    }

    @Test
    void saveHit_shouldReturnBadRequestWhenUriIsBlank() throws Exception {
        HitCreateDto invalidDto = new HitCreateDto(
                "ewm-main-service",
                "",
                "192.168.1.1",
                hourAgo
        );

        mvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(statsService, never()).saveHit(any());
    }

    @Test
    void saveHit_shouldReturnBadRequestWhenIpIsBlank() throws Exception {
        HitCreateDto invalidDto = new HitCreateDto(
                "ewm-main-service",
                "/events/1",
                "",
                hourAgo
        );

        mvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(statsService, never()).saveHit(any());
    }

    @Test
    void saveHit_shouldReturnBadRequestWhenTimestampIsNull() throws Exception {
        HitCreateDto invalidDto = new HitCreateDto(
                "ewm-main-service",
                "/events/1",
                "192.168.1.1",
                null
        );

        mvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(statsService, never()).saveHit(any());
    }

    @Test
    void saveHit_shouldReturnBadRequestWhenTimestampIsFuture() throws Exception {
        HitCreateDto invalidDto = new HitCreateDto(
                "ewm-main-service",
                "/events/1",
                "192.168.1.1",
                now.plusHours(1)
        );

        mvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(statsService, never()).saveHit(any());
    }

    @Test
    void getStats_shouldReturnStatsSuccessfully() throws Exception {
        List<ResponseStatsDto> expectedStats = List.of(
                new ResponseStatsDto("ewm-main-service", "/events/1", 5L),
                new ResponseStatsDto("ewm-main-service", "/events/2", 3L)
        );

        when(statsService.getStats(any(RequestStatsDto.class))).thenReturn(expectedStats);

        mvc.perform(get("/stats")
                        .param("start", twoHoursAgo.toString())
                        .param("end", now.toString())
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(5))
                .andExpect(jsonPath("$[1].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[1].uri").value("/events/2"))
                .andExpect(jsonPath("$[1].hits").value(3));

        verify(statsService, times(1)).getStats(any(RequestStatsDto.class));
    }

    @Test
    void getStats_shouldReturnStatsWithUrisFilter() throws Exception {
        List<ResponseStatsDto> expectedStats = List.of(
                new ResponseStatsDto("ewm-main-service", "/events/1", 5L)
        );

        when(statsService.getStats(any(RequestStatsDto.class))).thenReturn(expectedStats);

        mvc.perform(get("/stats")
                        .param("start", twoHoursAgo.toString())
                        .param("end", now.toString())
                        .param("uris", "/events/1", "/events/2")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uri").value("/events/1"));

        verify(statsService, times(1)).getStats(any(RequestStatsDto.class));
    }

    @Test
    void getStats_shouldReturnStatsWithUniqueTrue() throws Exception {
        List<ResponseStatsDto> expectedStats = List.of(
                new ResponseStatsDto("ewm-main-service", "/events/1", 3L)
        );

        when(statsService.getStats(any(RequestStatsDto.class))).thenReturn(expectedStats);

        mvc.perform(get("/stats")
                        .param("start", twoHoursAgo.toString())
                        .param("end", now.toString())
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hits").value(3));

        verify(statsService, times(1)).getStats(any(RequestStatsDto.class));
    }

    @Test
    void getStats_shouldReturnEmptyList() throws Exception {
        when(statsService.getStats(any(RequestStatsDto.class))).thenReturn(List.of());

        mvc.perform(get("/stats")
                        .param("start", twoHoursAgo.toString())
                        .param("end", now.toString())
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(statsService, times(1)).getStats(any(RequestStatsDto.class));
    }

    @Test
    void getStats_shouldUseDefaultUniqueValue() throws Exception {
        when(statsService.getStats(any(RequestStatsDto.class))).thenReturn(List.of());

        mvc.perform(get("/stats")
                        .param("start", twoHoursAgo.toString())
                        .param("end", now.toString()))
                .andExpect(status().isOk());

        verify(statsService, times(1)).getStats(argThat(request ->
                request.unique() != null && !request.unique()));
    }

    @Test
    void getStats_shouldReturnBadRequestWhenStartIsMissing() throws Exception {
        mvc.perform(get("/stats")
                        .param("end", now.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(statsService, never()).getStats(any());
    }

    @Test
    void getStats_shouldReturnBadRequestWhenEndIsMissing() throws Exception {
        mvc.perform(get("/stats")
                        .param("start", twoHoursAgo.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(statsService, never()).getStats(any());
    }

    @Test
    void getStats_shouldReturnBadRequestWhenDateRangeInvalid() throws Exception {
        mvc.perform(get("/stats")
                        .param("start", now.toString())
                        .param("end", twoHoursAgo.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата конца диапазона должна быть позже даты начала"));

        verify(statsService, never()).getStats(any());
    }

    @Test
    void getStats_shouldHandleEmptyUrisList() throws Exception {
        when(statsService.getStats(any(RequestStatsDto.class))).thenReturn(List.of());

        mvc.perform(get("/stats")
                        .param("start", twoHoursAgo.toString())
                        .param("end", now.toString())
                        .param("uris", ""))
                .andExpect(status().isOk());

        verify(statsService, times(1)).getStats(argThat(request ->
                request.uris() != null && request.uris().isEmpty()));
    }

    @Test
    void getStats_shouldHandleNullUris() throws Exception {
        when(statsService.getStats(any(RequestStatsDto.class))).thenReturn(List.of());

        mvc.perform(get("/stats")
                        .param("start", twoHoursAgo.toString())
                        .param("end", now.toString()))
                .andExpect(status().isOk());

        verify(statsService, times(1)).getStats(argThat(request ->
                request.uris() == null));
    }
}