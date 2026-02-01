package ru.practicum.client;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.request.RequestStatusUpdateParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@FeignClient(name = "request-service", dismiss404 = true, fallback = RequestClientFallback.class)
public interface RequestClient {
    @GetMapping("/admin/requests/{eventId}")
    List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long eventId);

    @PatchMapping("/admin/requests/status")
    @Headers(value = "Content-Type: application/json")
    @ResponseBody EventRequestStatusUpdateResult updateRequestsStatus(@RequestBody RequestStatusUpdateParam updateParams);

    @GetMapping("/admin/requests/count")
    Long countRequestsByEventAndStatus(@RequestParam Long eventId, @RequestParam RequestStatus status);

    @GetMapping("/admin/requests/countConfirmed")
    @Headers(value = "Content-Type: application/json")
    @ResponseBody Map<Long, Long> getConfirmedRequestsByEvents(@RequestBody List<Long> eventIds);

    @GetMapping("/admin/requests/{eventId}/{userId}")
    Optional<ParticipationRequestDto> getRequestByEventAndUser(@PathVariable("eventId") Long eventId, @PathVariable("userId") Long userId);
}
