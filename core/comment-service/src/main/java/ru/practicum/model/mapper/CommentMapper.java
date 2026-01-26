package ru.practicum.model.mapper;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.StateCommentDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentState;

import java.time.LocalDateTime;

public class CommentMapper {
    public static Comment mapToComment(NewCommentDto commentDto, Long authorId, Long eventId, CommentState state) {
        return Comment.builder()
                .authorId(authorId)
                .eventId(eventId)
                .text(commentDto.text())
                .state(state)
                .created(LocalDateTime.now())
                .build();
    }

    public static CommentDto mapToCommentDto(Comment comment, UserDto author) {
        return new CommentDto(comment.getId(), author.name(), comment.getText());
    }

    public static StateCommentDto mapToAdminDto(Comment comment, UserDto author) {
        return new StateCommentDto(comment.getId(), author.name(), comment.getText(), comment.getState());
    }
}