package ru.practicum.service;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.StateCommentDto;
import ru.practicum.dto.UpdateCommentDto;
import ru.practicum.model.CommentState;
import ru.practicum.model.DateSort;

import java.util.List;

public interface CommentService {
    CommentDto createComment(long userId, NewCommentDto commentDto);

    List<CommentDto> getComments(long userId);

    CommentDto updateComment(long userId, UpdateCommentDto commentDto);

    void deleteComment(long userId, long comId);

    List<StateCommentDto> getComments(String text, DateSort sort);

    StateCommentDto reviewComment(long comId, boolean approved);

    void deleteComment(long comId);

    List<CommentDto> getCommentsByState(CommentState state, DateSort sort);

    List<CommentDto> getCommentsByEvent(long eventId, DateSort sort);
}