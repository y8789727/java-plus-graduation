package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.request.RequestStatusUpdateParam;
import ru.practicum.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/requests")
@Slf4j
@RequiredArgsConstructor
public class AdminRequestController {
    private final RequestService requestService;

    @GetMapping("/{eventId}")
    List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long eventId) {
        return requestService.getRequestsByEvent(eventId);
    }

    @PatchMapping("/status")
    EventRequestStatusUpdateResult updateRequestsStatus(@RequestBody RequestStatusUpdateParam updateParams) {
        return requestService.updateRequestsStatus(updateParams);
    }

    @GetMapping("/count")
    Long countRequestsByEventAndStatus(@RequestParam Long eventId, @RequestParam RequestStatus status) {
        return requestService.countByEventAndStatus(eventId, status);
    }

    @GetMapping("/countConfirmed")
    @ResponseBody Map<Long, Long> getConfirmedRequestsByEvents(@RequestBody List<Long> eventIds) {
        return requestService.getConfirmedRequestsByEvents(eventIds);
    }
}
