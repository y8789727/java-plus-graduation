package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.CommentDto;
import ru.practicum.model.CommentState;
import ru.practicum.model.DateSort;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/comments")
    public List<CommentDto> getAllComments(@RequestParam(name = "sort", defaultValue = "ASC") DateSort sort) {
        log.info("Public: Get All comments, sort={}", sort);
        return commentService.getCommentsByState(CommentState.APPROVED, sort);
    }

    @GetMapping("/{eventId}/comments")
    public List<CommentDto> getEventComments(@PathVariable(name = "eventId") long eventId,
                                             @RequestParam(name = "sort", defaultValue = "ASC") DateSort sort) {
        log.info("Public: Get event ({}) comments sort={}", eventId, sort);
        return commentService.getCommentsByEvent(eventId, sort);
    }
}
